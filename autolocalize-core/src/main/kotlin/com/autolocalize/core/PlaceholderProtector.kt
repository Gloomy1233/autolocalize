package com.autolocalize.core

/**
 * Protects placeholders in text from being corrupted during translation.
 * 
 * Supports:
 * - Android format specifiers: %s, %d, %1$s, %2$d, etc.
 * - Named placeholders: {name}, {user_name}
 * - Template literals: ${name}, ${expression}
 * - HTML-like tags: <b>, </b>, <br/>, etc.
 */
class PlaceholderProtector {
    
    private val placeholderPatterns = listOf(
        // Android positional format specifiers: %1$s, %2$d, %1$,d, etc.
        Regex("""%(\d+\$)?[,+\-# 0(]*\d*\.?\d*[diouxXeEfFgGaAcsStTbBhHnp%]"""),
        // Named placeholders with braces: {name}, {user_name}, {0}
        Regex("""\{[a-zA-Z_][a-zA-Z0-9_]*\}"""),
        Regex("""\{\d+\}"""),
        // Template literals: ${name}, ${expression}
        Regex("""\$\{[^}]+\}"""),
        // HTML tags: <b>, </b>, <br/>, <tag attr="value">
        Regex("""</?[a-zA-Z][a-zA-Z0-9]*(?:\s+[^>]*)?>"""),
        // Self-closing tags
        Regex("""<[a-zA-Z][a-zA-Z0-9]*\s*/>""")
    )
    
    /**
     * Result of masking placeholders in text.
     */
    data class MaskResult(
        val maskedText: String,
        val placeholders: Map<String, String>
    )
    
    /**
     * Masks all placeholders in the text with unique tokens.
     * 
     * @param text The text containing placeholders
     * @return MaskResult containing the masked text and a map of tokens to original placeholders
     */
    fun mask(text: String): MaskResult {
        var maskedText = text
        val placeholders = mutableMapOf<String, String>()
        var tokenIndex = 0
        
        for (pattern in placeholderPatterns) {
            val matches = pattern.findAll(maskedText).toList()
            for (match in matches.reversed()) { // Process in reverse to maintain indices
                val placeholder = match.value
                // Check if this exact position hasn't already been masked
                if (maskedText.substring(match.range) == placeholder) {
                    val token = generateToken(tokenIndex++)
                    placeholders[token] = placeholder
                    maskedText = maskedText.replaceRange(match.range, token)
                }
            }
        }
        
        return MaskResult(maskedText, placeholders)
    }
    
    /**
     * Restores original placeholders from their masked tokens.
     * 
     * @param maskedText The text with masked placeholders
     * @param placeholders The map of tokens to original placeholders from mask()
     * @return The text with original placeholders restored
     */
    fun unmask(maskedText: String, placeholders: Map<String, String>): String {
        var result = maskedText
        
        // Sort by token index (descending) to handle overlapping replacements correctly
        val sortedEntries = placeholders.entries.sortedByDescending { 
            extractTokenIndex(it.key) 
        }
        
        for ((token, placeholder) in sortedEntries) {
            result = result.replace(token, placeholder)
        }
        
        return result
    }
    
    /**
     * Translates text while protecting placeholders.
     * 
     * @param text The text to translate
     * @param translateFn The translation function to apply
     * @return The translated text with original placeholders intact
     */
    suspend fun translateWithProtection(
        text: String,
        translateFn: suspend (String) -> String
    ): String {
        val (maskedText, placeholders) = mask(text)
        val translatedMasked = translateFn(maskedText)
        return unmask(translatedMasked, placeholders)
    }
    
    private fun generateToken(index: Int): String {
        // Use a format unlikely to appear in normal text or be modified by translation
        return "⟦PH$index⟧"
    }
    
    private fun extractTokenIndex(token: String): Int {
        return token.removePrefix("⟦PH").removeSuffix("⟧").toIntOrNull() ?: 0
    }
    
    companion object {
        /**
         * Singleton instance for convenience.
         */
        val instance = PlaceholderProtector()
    }
}

