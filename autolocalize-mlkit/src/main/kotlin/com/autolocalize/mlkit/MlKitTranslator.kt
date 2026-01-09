package com.autolocalize.mlkit

import com.autolocalize.core.*
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit-based translator for on-device translation.
 * 
 * Features:
 * - Lazy model downloading
 * - Progress tracking for model downloads
 * - Automatic caching of translators
 * - Graceful failure handling
 * 
 * Usage:
 * ```kotlin
 * val translator = MlKitTranslator()
 * 
 * // Check if ready
 * val ready = translator.isReady("en", "es")
 * 
 * // Prepare (download models if needed)
 * val result = translator.prepare("en", "es")
 * 
 * // Translate
 * val translated = translator.translate("Hello", "en", "es")
 * ```
 */
class MlKitTranslator(
    /**
     * Download conditions for language models.
     * Default requires Wi-Fi.
     */
    private val downloadConditions: DownloadConditions = DownloadConditions.Builder()
        .requireWifi()
        .build()
) : com.autolocalize.core.Translator {
    
    private val translatorCache = mutableMapOf<String, Translator>()
    private val mutex = Mutex()
    
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    
    /**
     * Current download state.
     */
    val downloadState: Flow<DownloadState> = _downloadState.asStateFlow()
    
    override suspend fun translate(
        text: String,
        sourceLanguageTag: String,
        targetLanguageTag: String,
        context: TranslationContext
    ): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext text
        
        val sourceCode = normalizeLanguageTag(sourceLanguageTag)
        val targetCode = normalizeLanguageTag(targetLanguageTag)
        
        if (sourceCode == targetCode) return@withContext text
        
        val mlKitSourceLang = toMlKitLanguage(sourceCode)
            ?: throw TranslationException("Unsupported source language: $sourceLanguageTag")
        val mlKitTargetLang = toMlKitLanguage(targetCode)
            ?: throw TranslationException("Unsupported target language: $targetLanguageTag")
        
        val translator = getOrCreateTranslator(mlKitSourceLang, mlKitTargetLang)
        
        try {
            suspendCancellableCoroutine { continuation ->
                translator.translate(text)
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(
                            TranslationException("Translation failed", exception)
                        )
                    }
            }
        } catch (e: Exception) {
            throw TranslationException("Translation failed: ${e.message}", e)
        }
    }
    
    override suspend fun isReady(
        sourceLanguageTag: String,
        targetLanguageTag: String
    ): Boolean = withContext(Dispatchers.IO) {
        val sourceCode = normalizeLanguageTag(sourceLanguageTag)
        val targetCode = normalizeLanguageTag(targetLanguageTag)
        
        val mlKitSourceLang = toMlKitLanguage(sourceCode) ?: return@withContext false
        val mlKitTargetLang = toMlKitLanguage(targetCode) ?: return@withContext false
        
        val modelManager = RemoteModelManager.getInstance()
        
        val sourceModel = TranslateRemoteModel.Builder(mlKitSourceLang).build()
        val targetModel = TranslateRemoteModel.Builder(mlKitTargetLang).build()
        
        val sourceDownloaded = suspendCancellableCoroutine { continuation ->
            modelManager.isModelDownloaded(sourceModel)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(false) }
        }
        
        val targetDownloaded = suspendCancellableCoroutine { continuation ->
            modelManager.isModelDownloaded(targetModel)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(false) }
        }
        
        sourceDownloaded && targetDownloaded
    }
    
    override suspend fun prepare(
        sourceLanguageTag: String,
        targetLanguageTag: String
    ): PrepareResult = withContext(Dispatchers.IO) {
        val sourceCode = normalizeLanguageTag(sourceLanguageTag)
        val targetCode = normalizeLanguageTag(targetLanguageTag)
        
        val mlKitSourceLang = toMlKitLanguage(sourceCode)
            ?: return@withContext PrepareResult.Failed(
                TranslationException("Unsupported source language: $sourceLanguageTag")
            )
        val mlKitTargetLang = toMlKitLanguage(targetCode)
            ?: return@withContext PrepareResult.Failed(
                TranslationException("Unsupported target language: $targetLanguageTag")
            )
        
        try {
            _downloadState.value = DownloadState.Downloading(0f, sourceLanguageTag)
            
            val translator = getOrCreateTranslator(mlKitSourceLang, mlKitTargetLang)

            return@withContext suspendCancellableCoroutine<PrepareResult> { continuation ->
                translator.downloadModelIfNeeded(downloadConditions)
                    .addOnSuccessListener {
                        _downloadState.value = DownloadState.Complete
                        continuation.resume(PrepareResult.Ready)
                    }
                    .addOnFailureListener { exception ->
                        _downloadState.value = DownloadState.Failed(exception)
                        continuation.resume(
                            PrepareResult.Failed(
                                ModelDownloadException(targetLanguageTag, cause = exception)
                            )
                        )
                    }
            }
        } catch (e: Exception) {
            _downloadState.value = DownloadState.Failed(e)
            return@withContext PrepareResult.Failed(TranslationException("Preparation failed: ${e.message}", e))
        }
    }
    
    override fun close() {
        translatorCache.values.forEach { it.close() }
        translatorCache.clear()
    }
    
    /**
     * Deletes downloaded models for a language.
     */
    suspend fun deleteModel(languageTag: String): Boolean = withContext(Dispatchers.IO) {
        val mlKitLang = toMlKitLanguage(normalizeLanguageTag(languageTag))
            ?: return@withContext false
        
        val model = TranslateRemoteModel.Builder(mlKitLang).build()
        val modelManager = RemoteModelManager.getInstance()
        
        suspendCancellableCoroutine { continuation ->
            modelManager.deleteDownloadedModel(model)
                .addOnSuccessListener { continuation.resume(true) }
                .addOnFailureListener { continuation.resume(false) }
        }
    }
    
    /**
     * Gets all downloaded language models.
     */
    suspend fun getDownloadedModels(): List<String> = withContext(Dispatchers.IO) {
        val modelManager = RemoteModelManager.getInstance()
        
        suspendCancellableCoroutine { continuation ->
            modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener { models ->
                    continuation.resume(models.map { it.language })
                }
                .addOnFailureListener {
                    continuation.resume(emptyList())
                }
        }
    }
    
    private suspend fun getOrCreateTranslator(
        sourceLang: String,
        targetLang: String
    ): Translator = mutex.withLock {
        val key = "${sourceLang}_$targetLang"
        translatorCache.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            Translation.getClient(options)
        }
    }
    
    private fun normalizeLanguageTag(tag: String): String {
        // Convert BCP 47 to simple language code
        return tag.split("-", "_").first().lowercase()
    }
    
    private fun toMlKitLanguage(code: String): String? {
        return LANGUAGE_CODE_MAP[code.lowercase()]
    }
    
    /**
     * State of model download.
     */
    sealed class DownloadState {
        object Idle : DownloadState()
        data class Downloading(val progress: Float, val language: String) : DownloadState()
        object Complete : DownloadState()
        data class Failed(val error: Throwable) : DownloadState()
    }
    
    companion object {
        /**
         * Map of BCP 47 language codes to ML Kit language codes.
         */
        private val LANGUAGE_CODE_MAP = mapOf(
            "af" to TranslateLanguage.AFRIKAANS,
            "ar" to TranslateLanguage.ARABIC,
            "be" to TranslateLanguage.BELARUSIAN,
            "bg" to TranslateLanguage.BULGARIAN,
            "bn" to TranslateLanguage.BENGALI,
            "ca" to TranslateLanguage.CATALAN,
            "cs" to TranslateLanguage.CZECH,
            "cy" to TranslateLanguage.WELSH,
            "da" to TranslateLanguage.DANISH,
            "de" to TranslateLanguage.GERMAN,
            "el" to TranslateLanguage.GREEK,
            "en" to TranslateLanguage.ENGLISH,
            "eo" to TranslateLanguage.ESPERANTO,
            "es" to TranslateLanguage.SPANISH,
            "et" to TranslateLanguage.ESTONIAN,
            "fa" to TranslateLanguage.PERSIAN,
            "fi" to TranslateLanguage.FINNISH,
            "fr" to TranslateLanguage.FRENCH,
            "ga" to TranslateLanguage.IRISH,
            "gl" to TranslateLanguage.GALICIAN,
            "gu" to TranslateLanguage.GUJARATI,
            "he" to TranslateLanguage.HEBREW,
            "hi" to TranslateLanguage.HINDI,
            "hr" to TranslateLanguage.CROATIAN,
            "ht" to TranslateLanguage.HAITIAN_CREOLE,
            "hu" to TranslateLanguage.HUNGARIAN,
            "id" to TranslateLanguage.INDONESIAN,
            "is" to TranslateLanguage.ICELANDIC,
            "it" to TranslateLanguage.ITALIAN,
            "ja" to TranslateLanguage.JAPANESE,
            "ka" to TranslateLanguage.GEORGIAN,
            "kn" to TranslateLanguage.KANNADA,
            "ko" to TranslateLanguage.KOREAN,
            "lt" to TranslateLanguage.LITHUANIAN,
            "lv" to TranslateLanguage.LATVIAN,
            "mk" to TranslateLanguage.MACEDONIAN,
            "mr" to TranslateLanguage.MARATHI,
            "ms" to TranslateLanguage.MALAY,
            "mt" to TranslateLanguage.MALTESE,
            "nl" to TranslateLanguage.DUTCH,
            "no" to TranslateLanguage.NORWEGIAN,
            "pl" to TranslateLanguage.POLISH,
            "pt" to TranslateLanguage.PORTUGUESE,
            "ro" to TranslateLanguage.ROMANIAN,
            "ru" to TranslateLanguage.RUSSIAN,
            "sk" to TranslateLanguage.SLOVAK,
            "sl" to TranslateLanguage.SLOVENIAN,
            "sq" to TranslateLanguage.ALBANIAN,
            "sv" to TranslateLanguage.SWEDISH,
            "sw" to TranslateLanguage.SWAHILI,
            "ta" to TranslateLanguage.TAMIL,
            "te" to TranslateLanguage.TELUGU,
            "th" to TranslateLanguage.THAI,
            "tl" to TranslateLanguage.TAGALOG,
            "tr" to TranslateLanguage.TURKISH,
            "uk" to TranslateLanguage.UKRAINIAN,
            "ur" to TranslateLanguage.URDU,
            "vi" to TranslateLanguage.VIETNAMESE,
            "zh" to TranslateLanguage.CHINESE
        )
        
        /**
         * Returns true if the language is supported by ML Kit.
         */
        fun isLanguageSupported(languageTag: String): Boolean {
            val code = languageTag.split("-", "_").first().lowercase()
            return LANGUAGE_CODE_MAP.containsKey(code)
        }
        
        /**
         * Returns list of all supported language codes.
         */
        fun getSupportedLanguages(): List<String> {
            return LANGUAGE_CODE_MAP.keys.toList()
        }
    }
}

