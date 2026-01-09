package com.autolocalize.core

/**
 * Interface for caching translations.
 * Implementations can use in-memory, disk, or remote storage.
 */
interface TranslationCache {
    /**
     * Retrieves a cached translation.
     *
     * @param key The cache key
     * @return The cached translation, or null if not found
     */
    suspend fun get(key: TranslationKey): String?
    
    /**
     * Stores a translation in the cache.
     *
     * @param key The cache key
     * @param value The translated text to cache
     */
    suspend fun put(key: TranslationKey, value: String)
    
    /**
     * Clears all cached translations.
     */
    suspend fun clear()
    
    /**
     * Removes a specific entry from the cache.
     */
    suspend fun remove(key: TranslationKey)
    
    /**
     * Returns the number of cached entries.
     */
    suspend fun size(): Int
}

/**
 * Key for identifying cached translations.
 */
data class TranslationKey(
    val sourceLanguageTag: String,
    val targetLanguageTag: String,
    val textHash: String,
    val context: TranslationContext
) {
    companion object {
        /**
         * Creates a TranslationKey from the given parameters.
         * Uses a hash of the text to keep keys compact.
         */
        fun create(
            text: String,
            sourceLanguageTag: String,
            targetLanguageTag: String,
            context: TranslationContext
        ): TranslationKey {
            return TranslationKey(
                sourceLanguageTag = sourceLanguageTag.lowercase(),
                targetLanguageTag = targetLanguageTag.lowercase(),
                textHash = text.hashCode().toString(16),
                context = context
            )
        }
    }
    
    /**
     * Returns a string representation suitable for use as a storage key.
     */
    fun toStorageKey(): String {
        return "${sourceLanguageTag}_${targetLanguageTag}_${context.name}_$textHash"
    }
}

/**
 * Cache policy configuration.
 */
data class CachePolicy(
    /**
     * Maximum number of entries in memory cache
     */
    val maxMemoryEntries: Int = 1000,
    
    /**
     * Whether to persist cache to disk
     */
    val persistToDisk: Boolean = true,
    
    /**
     * Time-to-live for cached entries in milliseconds.
     * -1 means entries never expire.
     */
    val ttlMillis: Long = -1,
    
    /**
     * Whether to cache failed translations
     */
    val cacheFailures: Boolean = false
) {
    companion object {
        val DEFAULT = CachePolicy()
        val MEMORY_ONLY = CachePolicy(persistToDisk = false)
        val NO_CACHE = CachePolicy(maxMemoryEntries = 0, persistToDisk = false)
    }
}

