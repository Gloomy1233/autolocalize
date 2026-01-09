package com.autolocalize.sample

import android.app.Application
import com.autolocalize.android.AutoLocalize
import com.autolocalize.android.AutoLocalizeConfig
import com.autolocalize.android.TranslateMode
import com.autolocalize.mlkit.MlKitTranslator
import com.autolocalize.views.AutoLocalizeViews

/**
 * Sample application demonstrating AutoLocalize initialization.
 */
class SampleApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AutoLocalize with configuration
        AutoLocalize.init(
            application = this,
            config = AutoLocalizeConfig.builder()
                // Source language for translation (text is assumed to be in this language)
                .sourceLocaleTag("en")
                // Supported locales for the language picker and validation
                .supportedLocales("en", "es", "fr", "de", "el", "ja", "zh")
                // Use ML Kit for on-device translation
                .translator(MlKitTranslator())
                // Enable locale persistence (default is true)
                .persistLocale(true)
                // Enable view tree translation for opt-in automatic translation
                .enableViewTreeTranslation(true)
                .viewTreeMode(TranslateMode.ONLY_TAGGED)
                // Protect placeholders like %s, {name}, etc. during translation
                .protectPlaceholders(true)
                .build()
        )
        
        // Install activity callbacks for automatic view tree translation
        // This will translate Views tagged with "al_translate" on activity resume
        AutoLocalizeViews.installActivityCallbacks(this)
    }
}

