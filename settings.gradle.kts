pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "autolocalize"

// Library modules
include(":autolocalize-core")
include(":autolocalize-android")
include(":autolocalize-views")
include(":autolocalize-mlkit")

// Sample app
include(":sample-app")

// Legacy modules (can be removed)
include(":app")
include(":autolocalization")
