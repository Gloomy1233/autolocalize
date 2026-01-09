package com.autolocalize.android

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.autolocalize.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Locale

/**
 * Main facade for the AutoLocalize library.
 * 
 * Usage:
 * ```
 * // In Application.onCreate()
 * AutoLocalize.init(this, AutoLocalizeConfig.builder()
 *     .sourceLocaleTag("en")
 *     .supportedLocales("en", "es", "fr", "de")
 *     .build())
 * 
 * // Set locale
 * AutoLocalize.setLocale("es")
 * 
 * // Translate text
 * val translated = AutoLocalize.translate("Hello, World!")
 * ```
 */
object AutoLocalize {
    
    private const val TAG = "AutoLocalize"
    
    private var _application: Application? = null
    private var _config: AutoLocalizeConfig = AutoLocalizeConfig()
    private var _localeStore: LocaleStore? = null
    private var _translator: Translator? = null
    private var _diskCache: DiskTranslationCache? = null
    
    private val _localeFlow = MutableStateFlow<Locale>(Locale.getDefault())
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    /**
     * Whether the library has been initialized.
     */
    val isInitialized: Boolean
        get() = _application != null
    
    /**
     * The current configuration.
     */
    val config: AutoLocalizeConfig
        get() = _config
    
    /**
     * Initializes the AutoLocalize library.
     * Call this in Application.onCreate().
     * 
     * @param application The Application instance
     * @param config Configuration options
     */
    fun init(application: Application, config: AutoLocalizeConfig = AutoLocalizeConfig()) {
        _application = application
        _config = config
        _localeStore = LocaleStore(application)
        
        // Set up logging
        Log.logger = AndroidLogger
        
        // Set up caching
        if (config.cachePolicy.persistToDisk) {
            _diskCache = DiskTranslationCache(
                context = application,
                maxMemoryEntries = config.cachePolicy.maxMemoryEntries
            )
        }
        
        // Wrap translator with caching if provided
        _translator = config.translator?.let { translator ->
            CachingTranslator(
                delegate = translator,
                cache = _diskCache ?: LruTranslationCache(config.cachePolicy.maxMemoryEntries),
                protectPlaceholders = config.protectPlaceholders
            )
        }
        
        // Restore persisted locale
        if (config.persistLocale) {
            scope.launch {
                try {
                    val savedLocale = _localeStore?.getLocale()
                    if (savedLocale != null) {
                        Log.d("Restoring saved locale: $savedLocale")
                        applyLocale(savedLocale, persist = false)
                    } else {
                        // Use system locale or first supported locale
                        val systemLocale = Locale.getDefault().toLanguageTag()
                        val effectiveLocale = if (config.supportedLocales.any { 
                            it.equals(systemLocale, ignoreCase = true) || 
                            systemLocale.startsWith(it, ignoreCase = true) 
                        }) {
                            systemLocale
                        } else {
                            config.supportedLocales.firstOrNull() ?: "en"
                        }
                        _localeFlow.value = Locale.forLanguageTag(effectiveLocale)
                    }
                } catch (e: Exception) {
                    Log.e("Failed to restore locale", e)
                }
            }
        }
        
        Log.i("AutoLocalize initialized with ${config.supportedLocales.size} supported locales")
    }
    
    /**
     * Sets the app's locale.
     * This triggers Android's per-app language preference system.
     * 
     * @param languageTag BCP 47 language tag (e.g., "en", "es", "fr-FR")
     */
    fun setLocale(languageTag: String) {
        checkInitialized()
        scope.launch {
            applyLocale(languageTag, persist = _config.persistLocale)
        }
    }
    
    /**
     * Sets the app's locale (suspend version).
     */
    suspend fun setLocaleSuspend(languageTag: String) {
        checkInitialized()
        applyLocale(languageTag, persist = _config.persistLocale)
    }
    
    /**
     * Gets the current locale tag.
     */
    fun getLocaleTag(): String {
        return _localeFlow.value.toLanguageTag()
    }
    
    /**
     * Gets the current Locale.
     */
    fun getLocale(): Locale {
        return _localeFlow.value
    }
    
    /**
     * Observes locale changes.
     */
    fun observeLocale(): Flow<Locale> {
        return _localeFlow.asStateFlow()
    }
    
    /**
     * Observes locale tag changes.
     */
    fun observeLocaleTag(): Flow<String> {
        return _localeFlow.map { it.toLanguageTag() }
    }
    
    /**
     * Translates text using the configured translator.
     * 
     * @param text The text to translate
     * @param context The translation context
     * @return The translated text, or the original if translation fails or no translator is configured
     */
    suspend fun translate(
        text: String,
        context: TranslationContext = TranslationContext.UI
    ): String {
        checkInitialized()
        
        val translator = _translator
        if (translator == null) {
            Log.w("No translator configured, returning original text")
            return text
        }
        
        val sourceTag = _config.sourceLocaleTag
        val targetTag = getLocaleTag()
        
        // Skip if source and target are the same
        if (sourceTag.equals(targetTag, ignoreCase = true) || 
            targetTag.startsWith(sourceTag, ignoreCase = true)) {
            return text
        }
        
        return try {
            translator.translate(text, sourceTag, targetTag, context)
        } catch (e: Exception) {
            Log.e("Translation failed for '$text'", e)
            text
        }
    }
    
    /**
     * Translates text asynchronously with a callback.
     */
    fun translateAsync(
        text: String,
        context: TranslationContext = TranslationContext.UI,
        callback: (String) -> Unit
    ) {
        scope.launch {
            val result = translate(text, context)
            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }
    
    /**
     * Sets a new translator implementation.
     */
    fun setTranslator(translator: Translator) {
        checkInitialized()
        _translator = CachingTranslator(
            delegate = translator,
            cache = _diskCache ?: LruTranslationCache(_config.cachePolicy.maxMemoryEntries),
            protectPlaceholders = _config.protectPlaceholders
        )
    }
    
    /**
     * Checks if the translator is ready for the current locale pair.
     */
    suspend fun isTranslatorReady(): Boolean {
        val translator = _translator ?: return false
        return translator.isReady(_config.sourceLocaleTag, getLocaleTag())
    }
    
    /**
     * Prepares the translator for the current locale pair (e.g., downloads models).
     */
    suspend fun prepareTranslator(): PrepareResult {
        val translator = _translator ?: return PrepareResult.Ready
        return translator.prepare(_config.sourceLocaleTag, getLocaleTag())
    }
    
    /**
     * Clears the translation cache.
     */
    suspend fun clearCache() {
        (_translator as? CachingTranslator)?.clearCache()
        _diskCache?.clear()
    }
    
    /**
     * Resets AutoLocalize to uninitialized state.
     * Mainly for testing purposes.
     */
    fun reset() {
        _application = null
        _config = AutoLocalizeConfig()
        _localeStore = null
        _translator?.close()
        _translator = null
        _diskCache = null
        _localeFlow.value = Locale.getDefault()
    }
    
    private suspend fun applyLocale(languageTag: String, persist: Boolean) {
        val locale = Locale.forLanguageTag(languageTag)
        
        // Update the locale using AppCompatDelegate
        withContext(Dispatchers.Main) {
            val localeList = LocaleListCompat.forLanguageTags(languageTag)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
        
        _localeFlow.value = locale
        
        // Persist if enabled
        if (persist) {
            _localeStore?.saveLocale(languageTag)
        }
        
        Log.i("Locale set to: $languageTag")
    }
    
    private fun checkInitialized() {
        check(_application != null) {
            "AutoLocalize not initialized. Call AutoLocalize.init(application, config) first."
        }
    }
}

