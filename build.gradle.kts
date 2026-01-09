// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    // Uncomment to enable Maven Central publishing
    // id("maven-publish") apply false
    // id("signing") apply false
}

// Uncomment to apply publishing configuration for Maven Central
// apply(from = "publishing.gradle.kts")
