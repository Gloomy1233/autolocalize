package com.autolocalize.views

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.autolocalize.android.AutoLocalize
import com.autolocalize.android.TranslateMode
import com.autolocalize.core.TranslationContext
import kotlinx.coroutines.*

/**
 * Helper object for automatic view tree translation.
 */
object AutoLocalizeViews {
    
    private const val TAG_TRANSLATE = "al_translate"
    private const val TAG_NO_TRANSLATE = "al_no_translate"
    private const val TAG_ORIGINAL_TEXT = "al_original_text"
    
    private var activityCallbacksInstalled = false
    private var translateMode: TranslateMode = TranslateMode.ONLY_TAGGED
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    /**
     * Translates a view tree based on the specified mode.
     * 
     * @param root The root view to start translation from
     * @param mode The translation mode (ONLY_TAGGED or EXCLUDE_TAGGED)
     */
    fun translateViewTree(root: View, mode: TranslateMode = TranslateMode.ONLY_TAGGED) {
        scope.launch {
            translateViewTreeSuspend(root, mode)
        }
    }
    
    /**
     * Translates a view tree (suspend version).
     */
    suspend fun translateViewTreeSuspend(root: View, mode: TranslateMode = TranslateMode.ONLY_TAGGED) {
        processView(root, mode)
    }
    
    /**
     * Reverts translated views to their original text.
     */
    fun revertViewTree(root: View) {
        revertView(root)
    }
    
    /**
     * Installs activity lifecycle callbacks for automatic translation.
     * Call this in Application.onCreate() after AutoLocalize.init().
     * 
     * Note: This feature is OFF by default and must be explicitly enabled
     * in AutoLocalizeConfig.enableViewTreeTranslation.
     */
    fun installActivityCallbacks(application: Application) {
        if (activityCallbacksInstalled) return
        
        if (!AutoLocalize.isInitialized) {
            throw IllegalStateException(
                "AutoLocalize must be initialized before installing activity callbacks"
            )
        }
        
        if (!AutoLocalize.config.enableViewTreeTranslation) {
            return // Feature is disabled
        }
        
        translateMode = AutoLocalize.config.viewTreeMode
        
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                val rootView = activity.window.decorView.rootView
                translateViewTree(rootView, translateMode)
            }
            
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        
        activityCallbacksInstalled = true
    }
    
    private suspend fun processView(view: View, mode: TranslateMode) {
        when (mode) {
            TranslateMode.ONLY_TAGGED -> {
                if (shouldTranslateOnlyTagged(view)) {
                    translateTextView(view as? TextView)
                }
            }
            TranslateMode.EXCLUDE_TAGGED -> {
                if (shouldTranslateExcludeTagged(view)) {
                    translateTextView(view as? TextView)
                }
            }
        }
        
        // Recursively process children
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                processView(view.getChildAt(i), mode)
            }
        }
    }
    
    private fun shouldTranslateOnlyTagged(view: View): Boolean {
        if (view !is TextView) return false
        
        val tag = view.tag
        return tag == TAG_TRANSLATE || 
               tag?.toString()?.contains(TAG_TRANSLATE) == true
    }
    
    private fun shouldTranslateExcludeTagged(view: View): Boolean {
        if (view !is TextView) return false
        
        val tag = view.tag
        if (tag == TAG_NO_TRANSLATE || 
            tag?.toString()?.contains(TAG_NO_TRANSLATE) == true) {
            return false
        }
        
        // Skip empty or numeric-only text
        val text = view.text?.toString() ?: return false
        if (text.isBlank() || text.all { it.isDigit() || it.isWhitespace() }) {
            return false
        }
        
        return true
    }
    
    private suspend fun translateTextView(textView: TextView?) {
        if (textView == null) return
        
        val originalText = textView.text?.toString() ?: return
        if (originalText.isBlank()) return
        
        // Skip if already translated (has original tag)
        val originalTag = textView.getTag(R.id.al_original_text_tag) as? String
        if (originalTag != null && originalTag == originalText) {
            // Already stored, just translate
        } else {
            // Store original text for reverting
            textView.setTag(R.id.al_original_text_tag, originalText)
        }
        
        // Translate
        try {
            if (!AutoLocalize.isInitialized) return
            
            val translated = AutoLocalize.translate(originalText, TranslationContext.UI)
            withContext(Dispatchers.Main) {
                if (translated != originalText) {
                    textView.text = translated
                }
            }
        } catch (e: Exception) {
            // Log error but keep original text
            android.util.Log.w("AutoLocalizeViews", "Translation failed for: $originalText", e)
        }
    }
    
    private fun revertView(view: View) {
        if (view is TextView) {
            val originalText = view.getTag(R.id.al_original_text_tag) as? String
            if (originalText != null) {
                view.text = originalText
            }
        }
        
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                revertView(view.getChildAt(i))
            }
        }
    }
}

