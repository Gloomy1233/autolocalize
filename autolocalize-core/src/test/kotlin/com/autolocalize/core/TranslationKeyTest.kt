package com.autolocalize.core

import org.junit.Assert.*
import org.junit.Test

class TranslationKeyTest {
    
    @Test
    fun `create generates consistent keys for same input`() {
        val key1 = TranslationKey.create(
            text = "Hello World",
            sourceLanguageTag = "en",
            targetLanguageTag = "es",
            context = TranslationContext.UI
        )
        
        val key2 = TranslationKey.create(
            text = "Hello World",
            sourceLanguageTag = "en",
            targetLanguageTag = "es",
            context = TranslationContext.UI
        )
        
        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())
        assertEquals(key1.toStorageKey(), key2.toStorageKey())
    }
    
    @Test
    fun `create generates different keys for different text`() {
        val key1 = TranslationKey.create(
            text = "Hello World",
            sourceLanguageTag = "en",
            targetLanguageTag = "es",
            context = TranslationContext.UI
        )
        
        val key2 = TranslationKey.create(
            text = "Goodbye World",
            sourceLanguageTag = "en",
            targetLanguageTag = "es",
            context = TranslationContext.UI
        )
        
        assertNotEquals(key1, key2)
        assertNotEquals(key1.toStorageKey(), key2.toStorageKey())
    }
    
    @Test
    fun `create generates different keys for different languages`() {
        val key1 = TranslationKey.create(
            text = "Hello World",
            sourceLanguageTag = "en",
            targetLanguageTag = "es",
            context = TranslationContext.UI
        )
        
        val key2 = TranslationKey.create(
            text = "Hello World",
            sourceLanguageTag = "en",
            targetLanguageTag = "fr",
            context = TranslationContext.UI
        )
        
        assertNotEquals(key1, key2)
    }
    
    @Test
    fun `create generates different keys for different contexts`() {
        val key1 = TranslationKey.create(
            text = "Hello World",
            sourceLanguageTag = "en",
            targetLanguageTag = "es",
            context = TranslationContext.UI
        )
        
        val key2 = TranslationKey.create(
            text = "Hello World",
            sourceLanguageTag = "en",
            targetLanguageTag = "es",
            context = TranslationContext.BACKEND
        )
        
        assertNotEquals(key1, key2)
    }
    
    @Test
    fun `create normalizes language tags to lowercase`() {
        val key1 = TranslationKey.create(
            text = "Hello",
            sourceLanguageTag = "EN",
            targetLanguageTag = "ES",
            context = TranslationContext.UI
        )
        
        val key2 = TranslationKey.create(
            text = "Hello",
            sourceLanguageTag = "en",
            targetLanguageTag = "es",
            context = TranslationContext.UI
        )
        
        assertEquals(key1, key2)
    }
    
    @Test
    fun `toStorageKey produces valid string`() {
        val key = TranslationKey.create(
            text = "Test",
            sourceLanguageTag = "en",
            targetLanguageTag = "es",
            context = TranslationContext.UI
        )
        
        val storageKey = key.toStorageKey()
        
        assertTrue(storageKey.contains("en"))
        assertTrue(storageKey.contains("es"))
        assertTrue(storageKey.contains("UI"))
        assertFalse(storageKey.contains(" "))
    }
}

