package com.autolocalize.core

/**
 * Simple logging interface for AutoLocalize.
 * Implement this to integrate with your logging framework.
 */
interface AutoLocalizeLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Default no-op logger.
 */
object NoOpLogger : AutoLocalizeLogger {
    override fun debug(tag: String, message: String) {}
    override fun info(tag: String, message: String) {}
    override fun warn(tag: String, message: String, throwable: Throwable?) {}
    override fun error(tag: String, message: String, throwable: Throwable?) {}
}

/**
 * Simple console logger for debugging.
 */
object ConsoleLogger : AutoLocalizeLogger {
    override fun debug(tag: String, message: String) {
        println("D/$tag: $message")
    }
    
    override fun info(tag: String, message: String) {
        println("I/$tag: $message")
    }
    
    override fun warn(tag: String, message: String, throwable: Throwable?) {
        println("W/$tag: $message")
        throwable?.printStackTrace()
    }
    
    override fun error(tag: String, message: String, throwable: Throwable?) {
        System.err.println("E/$tag: $message")
        throwable?.printStackTrace()
    }
}

/**
 * Internal logging helper.
 * Made public to allow Android module to set the logger.
 */
object Log {
    private const val TAG = "AutoLocalize"
    
    var logger: AutoLocalizeLogger = NoOpLogger
    
    fun d(message: String) = logger.debug(TAG, message)
    fun i(message: String) = logger.info(TAG, message)
    fun w(message: String, t: Throwable? = null) = logger.warn(TAG, message, t)
    fun e(message: String, t: Throwable? = null) = logger.error(TAG, message, t)
}

