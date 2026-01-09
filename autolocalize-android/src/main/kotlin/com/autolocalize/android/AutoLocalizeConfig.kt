package com.autolocalize.android

import com.autolocalize.core.CachePolicy
import com.autolocalize.core.Translator

/**
 * Configuration for AutoLocalize library.
 */
data class AutoLocalizeConfig(
    /**
     * The source locale tag for translation (e.g., "en").
     * Text is assumed to be in this language when translating.
     */
    val sourceLocaleTag: String = "en",
    
    /**
     * List of supported locale tags.
     * Used for validation and UI components.
     */
    val supportedLocales: List<String> = listOf("en"),
    
    /**
     * Optional translator for runtime translation of non-resource text.
     * If null, only resource-based localization is available.
     */
    val translator: Translator? = null,
    
    /**
     * Cache policy for translations.
     */
    val cachePolicy: CachePolicy = CachePolicy.DEFAULT,
    
    /**
     * Whether to enable automatic view tree translation.
     * When enabled, Views can be automatically translated based on tags.
     */
    val enableViewTreeTranslation: Boolean = false,
    
    /**
     * Mode for view tree translation.
     * Only applies when enableViewTreeTranslation is true.
     */
    val viewTreeMode: TranslateMode = TranslateMode.ONLY_TAGGED,
    
    /**
     * Whether to persist the selected locale to DataStore.
     */
    val persistLocale: Boolean = true,
    
    /**
     * Whether to protect placeholders during translation.
     */
    val protectPlaceholders: Boolean = true
) {
    
    /**
     * Builder for AutoLocalizeConfig.
     */
    class Builder {
        private var sourceLocaleTag: String = "en"
        private var supportedLocales: List<String> = listOf("en")
        private var translator: Translator? = null
        private var cachePolicy: CachePolicy = CachePolicy.DEFAULT
        private var enableViewTreeTranslation: Boolean = false
        private var viewTreeMode: TranslateMode = TranslateMode.ONLY_TAGGED
        private var persistLocale: Boolean = true
        private var protectPlaceholders: Boolean = true
        
        fun sourceLocaleTag(tag: String) = apply { this.sourceLocaleTag = tag }
        fun supportedLocales(locales: List<String>) = apply { this.supportedLocales = locales }
        fun supportedLocales(vararg locales: String) = apply { this.supportedLocales = locales.toList() }
        fun translator(translator: Translator?) = apply { this.translator = translator }
        fun cachePolicy(policy: CachePolicy) = apply { this.cachePolicy = policy }
        fun enableViewTreeTranslation(enable: Boolean) = apply { this.enableViewTreeTranslation = enable }
        fun viewTreeMode(mode: TranslateMode) = apply { this.viewTreeMode = mode }
        fun persistLocale(persist: Boolean) = apply { this.persistLocale = persist }
        fun protectPlaceholders(protect: Boolean) = apply { this.protectPlaceholders = protect }
        
        fun build() = AutoLocalizeConfig(
            sourceLocaleTag = sourceLocaleTag,
            supportedLocales = supportedLocales,
            translator = translator,
            cachePolicy = cachePolicy,
            enableViewTreeTranslation = enableViewTreeTranslation,
            viewTreeMode = viewTreeMode,
            persistLocale = persistLocale,
            protectPlaceholders = protectPlaceholders
        )
    }
    
    companion object {
        fun builder() = Builder()
    }
}

/**
 * Mode for view tree translation.
 */
enum class TranslateMode {
    /**
     * Only translate Views with the "al_translate" tag or al_translate="true" attribute.
     */
    ONLY_TAGGED,
    
    /**
     * Translate all Views except those with "al_no_translate" tag.
     */
    EXCLUDE_TAGGED
}

