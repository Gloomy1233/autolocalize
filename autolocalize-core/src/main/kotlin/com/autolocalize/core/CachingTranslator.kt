package com.autolocalize.core

/**
 * A translator wrapper that adds caching and placeholder protection.
 */
class CachingTranslator(
    private val delegate: Translator,
    private val cache: TranslationCache = LruTranslationCache(),
    private val placeholderProtector: PlaceholderProtector = PlaceholderProtector.instance,
    private val protectPlaceholders: Boolean = true
) : Translator {
    
    override suspend fun translate(
        text: String,
        sourceLanguageTag: String,
        targetLanguageTag: String,
        context: TranslationContext
    ): String {
        // Skip translation if source and target are the same
        if (sourceLanguageTag.equals(targetLanguageTag, ignoreCase = true)) {
            return text
        }
        
        // Skip empty or whitespace-only text
        if (text.isBlank()) {
            return text
        }
        
        // Check cache first
        val cacheKey = TranslationKey.create(text, sourceLanguageTag, targetLanguageTag, context)
        cache.get(cacheKey)?.let { cached ->
            return cached
        }
        
        // Translate with placeholder protection
        val translated = if (protectPlaceholders) {
            placeholderProtector.translateWithProtection(text) { maskedText ->
                delegate.translate(maskedText, sourceLanguageTag, targetLanguageTag, context)
            }
        } else {
            delegate.translate(text, sourceLanguageTag, targetLanguageTag, context)
        }
        
        // Cache the result
        cache.put(cacheKey, translated)
        
        return translated
    }
    
    override suspend fun isReady(sourceLanguageTag: String, targetLanguageTag: String): Boolean {
        return delegate.isReady(sourceLanguageTag, targetLanguageTag)
    }
    
    override suspend fun prepare(sourceLanguageTag: String, targetLanguageTag: String): PrepareResult {
        return delegate.prepare(sourceLanguageTag, targetLanguageTag)
    }
    
    override fun close() {
        delegate.close()
    }
    
    /**
     * Clears the translation cache.
     */
    suspend fun clearCache() {
        cache.clear()
    }
}

