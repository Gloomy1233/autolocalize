package com.autolocalize.core

import org.junit.Assert.*
import org.junit.Test

class PlaceholderProtectorTest {
    
    private val protector = PlaceholderProtector()
    
    @Test
    fun `mask and unmask simple format specifiers`() {
        val text = "Hello %s, you have %d messages"
        val (masked, placeholders) = protector.mask(text)
        
        // Placeholders should be masked
        assertFalse(masked.contains("%s"))
        assertFalse(masked.contains("%d"))
        assertEquals(2, placeholders.size)
        
        // Unmask should restore original
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
    
    @Test
    fun `mask and unmask positional format specifiers`() {
        val text = "Welcome %1\$s! Your balance is %2\$,d"
        val (masked, placeholders) = protector.mask(text)
        
        assertFalse(masked.contains("%1\$s"))
        assertFalse(masked.contains("%2\$,d"))
        
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
    
    @Test
    fun `mask and unmask named placeholders`() {
        val text = "Hello {name}, welcome to {city}!"
        val (masked, placeholders) = protector.mask(text)
        
        assertFalse(masked.contains("{name}"))
        assertFalse(masked.contains("{city}"))
        assertEquals(2, placeholders.size)
        
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
    
    @Test
    fun `mask and unmask template literals`() {
        val text = "Hello \${user.name}, your order \${orderId} is ready"
        val (masked, placeholders) = protector.mask(text)
        
        assertFalse(masked.contains("\${user.name}"))
        assertFalse(masked.contains("\${orderId}"))
        
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
    
    @Test
    fun `mask and unmask HTML tags`() {
        val text = "<b>Important:</b> Please read the <a href=\"#\">terms</a>"
        val (masked, placeholders) = protector.mask(text)
        
        assertFalse(masked.contains("<b>"))
        assertFalse(masked.contains("</b>"))
        assertFalse(masked.contains("</a>"))
        
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
    
    @Test
    fun `mask and unmask mixed placeholders`() {
        val text = "User %1\$s ({userId}) ordered %2\$d items for \${price}"
        val (masked, placeholders) = protector.mask(text)
        
        assertFalse(masked.contains("%1\$s"))
        assertFalse(masked.contains("{userId}"))
        assertFalse(masked.contains("%2\$d"))
        assertFalse(masked.contains("\${price}"))
        
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
    
    @Test
    fun `mask handles text without placeholders`() {
        val text = "Hello, this is plain text"
        val (masked, placeholders) = protector.mask(text)
        
        assertEquals(text, masked)
        assertTrue(placeholders.isEmpty())
    }
    
    @Test
    fun `mask handles empty text`() {
        val text = ""
        val (masked, placeholders) = protector.mask(text)
        
        assertEquals("", masked)
        assertTrue(placeholders.isEmpty())
    }
    
    @Test
    fun `unmask with modified translated text`() {
        val original = "You have %d new messages"
        val (masked, placeholders) = protector.mask(original)
        
        // Simulate translation that changes word order
        val translatedMasked = masked.replace("You have", "Tienes").replace("new messages", "mensajes nuevos")
        
        val result = protector.unmask(translatedMasked, placeholders)
        assertTrue(result.contains("%d"))
    }
    
    @Test
    fun `mask handles numbered placeholders`() {
        val text = "Step {0}: Complete {1} tasks"
        val (masked, placeholders) = protector.mask(text)
        
        assertFalse(masked.contains("{0}"))
        assertFalse(masked.contains("{1}"))
        assertEquals(2, placeholders.size)
        
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
    
    @Test
    fun `mask preserves percent literal`() {
        val text = "100%% complete with %d items"
        val (masked, placeholders) = protector.mask(text)
        
        // %% should be treated as format specifier (literal %)
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
    
    @Test
    fun `mask handles float format specifiers`() {
        val text = "Price: %1\$.2f USD"
        val (masked, placeholders) = protector.mask(text)
        
        assertFalse(masked.contains("%1\$.2f"))
        
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
    
    @Test
    fun `mask handles self-closing HTML tags`() {
        val text = "Line 1<br/>Line 2"
        val (masked, placeholders) = protector.mask(text)
        
        assertFalse(masked.contains("<br/>"))
        
        val restored = protector.unmask(masked, placeholders)
        assertEquals(text, restored)
    }
}

