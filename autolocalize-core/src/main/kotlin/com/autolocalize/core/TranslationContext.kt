package com.autolocalize.core

/**
 * Context in which translation is being performed.
 * This can be used by translators and caches to apply different
 * strategies based on the type of content being translated.
 */
enum class TranslationContext {
    /**
     * UI strings - typically short, may contain placeholders
     */
    UI,
    
    /**
     * Backend content - API responses, server messages
     */
    BACKEND,
    
    /**
     * User-generated content - comments, posts, messages
     */
    USER_CONTENT,
    
    /**
     * System messages - errors, notifications
     */
    SYSTEM
}

