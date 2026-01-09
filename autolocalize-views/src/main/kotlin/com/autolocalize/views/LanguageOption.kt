package com.autolocalize.views

import java.util.Locale

/**
 * Represents a language option in the picker.
 */
data class LanguageOption(
    /**
     * BCP 47 language tag (e.g., "en", "es", "fr-FR")
     */
    val tag: String,
    
    /**
     * Display label for the language (e.g., "English", "Spanish")
     */
    val label: String,
    
    /**
     * Native label for the language (e.g., "English", "Español")
     */
    val nativeLabel: String? = null,
    
    /**
     * How to show the label in the picker
     */
    val showLabelMode: ShowLabelMode = ShowLabelMode.DEFAULT
) {
    /**
     * Returns the appropriate display text based on the showLabelMode.
     */
    fun getDisplayText(overrideMode: ShowLabelMode? = null): String {
        return when (overrideMode ?: showLabelMode) {
            ShowLabelMode.DISPLAY_NAME -> label
            ShowLabelMode.NATIVE_NAME -> nativeLabel ?: label
            ShowLabelMode.CUSTOM_LABEL -> label
            ShowLabelMode.DEFAULT -> label
        }
    }
    
    companion object {
        /**
         * Creates a LanguageOption from a language tag, auto-generating labels.
         */
        fun fromTag(tag: String): LanguageOption {
            val locale = Locale.forLanguageTag(tag)
            val displayName = locale.getDisplayName(Locale.getDefault())
                .replaceFirstChar { it.uppercase() }
            val nativeName = locale.getDisplayName(locale)
                .replaceFirstChar { it.uppercase() }
            
            return LanguageOption(
                tag = tag,
                label = displayName,
                nativeLabel = nativeName
            )
        }
        
        /**
         * Creates a list of LanguageOptions from language tags.
         */
        fun fromTags(vararg tags: String): List<LanguageOption> {
            return tags.map { fromTag(it) }
        }
        
        /**
         * Creates a list of LanguageOptions from language tags.
         */
        fun fromTags(tags: List<String>): List<LanguageOption> {
            return tags.map { fromTag(it) }
        }
        
        /**
         * Common language options.
         */
        val COMMON_LANGUAGES = listOf(
            LanguageOption("en", "English", "English"),
            LanguageOption("es", "Spanish", "Español"),
            LanguageOption("fr", "French", "Français"),
            LanguageOption("de", "German", "Deutsch"),
            LanguageOption("it", "Italian", "Italiano"),
            LanguageOption("pt", "Portuguese", "Português"),
            LanguageOption("zh", "Chinese", "中文"),
            LanguageOption("ja", "Japanese", "日本語"),
            LanguageOption("ko", "Korean", "한국어"),
            LanguageOption("ar", "Arabic", "العربية"),
            LanguageOption("ru", "Russian", "Русский"),
            LanguageOption("hi", "Hindi", "हिन्दी"),
            LanguageOption("el", "Greek", "Ελληνικά"),
            LanguageOption("tr", "Turkish", "Türkçe"),
            LanguageOption("nl", "Dutch", "Nederlands"),
            LanguageOption("pl", "Polish", "Polski"),
            LanguageOption("vi", "Vietnamese", "Tiếng Việt"),
            LanguageOption("th", "Thai", "ไทย"),
            LanguageOption("id", "Indonesian", "Bahasa Indonesia"),
            LanguageOption("uk", "Ukrainian", "Українська")
        )
    }
}

/**
 * How to display the language label.
 */
enum class ShowLabelMode {
    /**
     * Use the default behavior (same as DISPLAY_NAME)
     */
    DEFAULT,
    
    /**
     * Show language name in the current locale (e.g., "Spanish" when English)
     */
    DISPLAY_NAME,
    
    /**
     * Show language name in its native form (e.g., "Español")
     */
    NATIVE_NAME,
    
    /**
     * Show custom label provided in LanguageOption
     */
    CUSTOM_LABEL
}

/**
 * Display mode for the language picker.
 */
enum class DisplayMode {
    /**
     * Show only language name
     */
    NAME_ONLY,
    
    /**
     * Show only flag emoji (if available)
     */
    FLAG_ONLY,
    
    /**
     * Show flag emoji + language name
     */
    FLAG_AND_NAME
}

