package com.autolocalize.core

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe in-memory LRU cache for translations.
 */
class LruTranslationCache(
    private val maxSize: Int = 1000
) : TranslationCache {
    
    private val cache = LinkedHashMap<String, CacheEntry>(16, 0.75f, true)
    private val mutex = Mutex()
    
    private data class CacheEntry(
        val value: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    override suspend fun get(key: TranslationKey): String? = mutex.withLock {
        cache[key.toStorageKey()]?.value
    }
    
    override suspend fun put(key: TranslationKey, value: String) = mutex.withLock {
        val storageKey = key.toStorageKey()
        cache[storageKey] = CacheEntry(value)
        
        // Evict oldest entries if over capacity
        while (cache.size > maxSize) {
            val oldestKey = cache.keys.firstOrNull() ?: break
            cache.remove(oldestKey)
        }
    }
    
    override suspend fun clear() = mutex.withLock {
        cache.clear()
    }
    
    override suspend fun remove(key: TranslationKey) = mutex.withLock {
        cache.remove(key.toStorageKey())
        Unit
    }
    
    override suspend fun size(): Int = mutex.withLock {
        cache.size
    }
}

