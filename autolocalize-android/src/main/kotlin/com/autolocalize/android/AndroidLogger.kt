package com.autolocalize.android

import android.util.Log
import com.autolocalize.core.AutoLocalizeLogger

/**
 * Android-specific logger implementation using android.util.Log.
 */
object AndroidLogger : AutoLocalizeLogger {
    
    var isEnabled: Boolean = true
    
    override fun debug(tag: String, message: String) {
        if (isEnabled) {
            Log.d(tag, message)
        }
    }
    
    override fun info(tag: String, message: String) {
        if (isEnabled) {
            Log.i(tag, message)
        }
    }
    
    override fun warn(tag: String, message: String, throwable: Throwable?) {
        if (isEnabled) {
            if (throwable != null) {
                Log.w(tag, message, throwable)
            } else {
                Log.w(tag, message)
            }
        }
    }
    
    override fun error(tag: String, message: String, throwable: Throwable?) {
        if (isEnabled) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }
}

