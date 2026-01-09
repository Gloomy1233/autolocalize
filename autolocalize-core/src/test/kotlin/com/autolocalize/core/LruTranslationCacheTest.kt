package com.autolocalize.core

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LruTranslationCacheTest {
    
    @Test
    fun `put and get returns cached value`() = runTest {
        val cache = LruTranslationCache(maxSize = 100)
        val key = TranslationKey.create("Hello", "en", "es", TranslationContext.UI)
        
        cache.put(key, "Hola")
        
        assertEquals("Hola", cache.get(key))
    }
    
    @Test
    fun `get returns null for missing key`() = runTest {
        val cache = LruTranslationCache(maxSize = 100)
        val key = TranslationKey.create("Hello", "en", "es", TranslationContext.UI)
        
        assertNull(cache.get(key))
    }
    
    @Test
    fun `cache evicts oldest entries when full`() = runTest {
        val cache = LruTranslationCache(maxSize = 3)
        
        val key1 = TranslationKey.create("One", "en", "es", TranslationContext.UI)
        val key2 = TranslationKey.create("Two", "en", "es", TranslationContext.UI)
        val key3 = TranslationKey.create("Three", "en", "es", TranslationContext.UI)
        val key4 = TranslationKey.create("Four", "en", "es", TranslationContext.UI)
        
        cache.put(key1, "Uno")
        cache.put(key2, "Dos")
        cache.put(key3, "Tres")
        cache.put(key4, "Cuatro")
        
        // key1 should have been evicted
        assertNull(cache.get(key1))
        assertEquals("Dos", cache.get(key2))
        assertEquals("Tres", cache.get(key3))
        assertEquals("Cuatro", cache.get(key4))
    }
    
    @Test
    fun `clear removes all entries`() = runTest {
        val cache = LruTranslationCache(maxSize = 100)
        val key = TranslationKey.create("Hello", "en", "es", TranslationContext.UI)
        
        cache.put(key, "Hola")
        cache.clear()
        
        assertNull(cache.get(key))
        assertEquals(0, cache.size())
    }
    
    @Test
    fun `remove deletes specific entry`() = runTest {
        val cache = LruTranslationCache(maxSize = 100)
        val key1 = TranslationKey.create("Hello", "en", "es", TranslationContext.UI)
        val key2 = TranslationKey.create("World", "en", "es", TranslationContext.UI)
        
        cache.put(key1, "Hola")
        cache.put(key2, "Mundo")
        cache.remove(key1)
        
        assertNull(cache.get(key1))
        assertEquals("Mundo", cache.get(key2))
    }
    
    @Test
    fun `size returns correct count`() = runTest {
        val cache = LruTranslationCache(maxSize = 100)
        
        assertEquals(0, cache.size())
        
        cache.put(TranslationKey.create("One", "en", "es", TranslationContext.UI), "Uno")
        assertEquals(1, cache.size())
        
        cache.put(TranslationKey.create("Two", "en", "es", TranslationContext.UI), "Dos")
        assertEquals(2, cache.size())
    }
    
    @Test
    fun `lru access order is maintained`() = runTest {
        val cache = LruTranslationCache(maxSize = 3)
        
        val key1 = TranslationKey.create("One", "en", "es", TranslationContext.UI)
        val key2 = TranslationKey.create("Two", "en", "es", TranslationContext.UI)
        val key3 = TranslationKey.create("Three", "en", "es", TranslationContext.UI)
        val key4 = TranslationKey.create("Four", "en", "es", TranslationContext.UI)
        
        cache.put(key1, "Uno")
        cache.put(key2, "Dos")
        cache.put(key3, "Tres")
        
        // Access key1 to make it recently used
        cache.get(key1)
        
        // Add key4, should evict key2 (least recently used)
        cache.put(key4, "Cuatro")
        
        assertEquals("Uno", cache.get(key1))  // Still present (was accessed)
        assertNull(cache.get(key2))           // Evicted
        assertEquals("Tres", cache.get(key3)) // Still present
        assertEquals("Cuatro", cache.get(key4)) // Newly added
    }
}

