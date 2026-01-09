package com.autolocalize.core

/**
 * Core interface for translation services.
 * Implementations can use ML Kit, cloud APIs, or custom translation logic.
 */
interface Translator {
    /**
     * Translates text from source language to target language.
     *
     * @param text The text to translate
     * @param sourceLanguageTag BCP 47 language tag of the source language (e.g., "en", "es")
     * @param targetLanguageTag BCP 47 language tag of the target language
     * @param context The context in which translation is being performed
     * @return The translated text
     * @throws TranslationException if translation fails
     */
    suspend fun translate(
        text: String,
        sourceLanguageTag: String,
        targetLanguageTag: String,
        context: TranslationContext = TranslationContext.UI
    ): String
    
    /**
     * Checks if the translator is ready to translate between the given language pair.
     * For on-device translators, this may involve checking if models are downloaded.
     */
    suspend fun isReady(
        sourceLanguageTag: String,
        targetLanguageTag: String
    ): Boolean = true
    
    /**
     * Prepares the translator for the given language pair.
     * For on-device translators, this may trigger model downloads.
     */
    suspend fun prepare(
        sourceLanguageTag: String,
        targetLanguageTag: String
    ): PrepareResult = PrepareResult.Ready
    
    /**
     * Releases any resources held by the translator.
     */
    fun close() {}
}

/**
 * Result of preparing a translator for a language pair.
 */
sealed class PrepareResult {
    /** The translator is ready to use */
    object Ready : PrepareResult()
    
    /** Model download is in progress */
    data class Downloading(val progress: Float) : PrepareResult()
    
    /** Preparation failed */
    data class Failed(val error: TranslationException) : PrepareResult()
}

/**
 * Exception thrown when translation fails.
 */
open class TranslationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when a required language model is not available.
 */
class ModelNotAvailableException(
    val languageTag: String,
    message: String = "Language model not available for: $languageTag",
    cause: Throwable? = null
) : TranslationException(message, cause)

/**
 * Exception thrown when model download fails.
 */
class ModelDownloadException(
    val languageTag: String,
    message: String = "Failed to download language model for: $languageTag",
    cause: Throwable? = null
) : TranslationException(message, cause)

