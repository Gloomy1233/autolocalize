package com.autolocalize.android

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.autolocalize.core.TranslationCache
import com.autolocalize.core.TranslationKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * DataStore instance for translation cache.
 */
private val Context.translationCacheDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "autolocalize_translation_cache"
)

/**
 * Disk-backed translation cache using DataStore.
 * Also maintains an in-memory LRU cache for faster access.
 */
class DiskTranslationCache(
    private val context: Context,
    private val maxMemoryEntries: Int = 500
) : TranslationCache {
    
    private val memoryCache = LinkedHashMap<String, String>(16, 0.75f, true)
    private val mutex = Mutex()
    
    override suspend fun get(key: TranslationKey): String? = mutex.withLock {
        val storageKey = key.toStorageKey()
        
        // Check memory cache first
        memoryCache[storageKey]?.let { return@withLock it }
        
        // Fall back to disk cache
        val prefKey = stringPreferencesKey(storageKey)
        val diskValue = context.translationCacheDataStore.data.first()[prefKey]
        
        // Populate memory cache if found on disk
        if (diskValue != null) {
            addToMemoryCache(storageKey, diskValue)
        }
        
        return@withLock diskValue
    }
    
    override suspend fun put(key: TranslationKey, value: String): Unit = mutex.withLock {
        val storageKey = key.toStorageKey()
        
        // Add to memory cache
        addToMemoryCache(storageKey, value)
        
        // Persist to disk
        val prefKey = stringPreferencesKey(storageKey)
        context.translationCacheDataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }
    
    override suspend fun clear(): Unit = mutex.withLock {
        memoryCache.clear()
        context.translationCacheDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    override suspend fun remove(key: TranslationKey): Unit = mutex.withLock {
        val storageKey = key.toStorageKey()
        memoryCache.remove(storageKey)
        
        val prefKey = stringPreferencesKey(storageKey)
        context.translationCacheDataStore.edit { preferences ->
            preferences.remove(prefKey)
        }
    }
    
    override suspend fun size(): Int = mutex.withLock {
        context.translationCacheDataStore.data.first().asMap().size
    }
    
    /**
     * Returns the number of entries in memory cache.
     */
    suspend fun memoryCacheSize(): Int = mutex.withLock {
        memoryCache.size
    }
    
    private fun addToMemoryCache(key: String, value: String) {
        memoryCache[key] = value
        
        // Evict oldest entries if over capacity
        while (memoryCache.size > maxMemoryEntries) {
            val oldestKey = memoryCache.keys.firstOrNull() ?: break
            memoryCache.remove(oldestKey)
        }
    }
}

