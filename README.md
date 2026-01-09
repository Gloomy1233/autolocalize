# AutoLocalize

A production-quality Android library for comprehensive app localization, featuring instant locale switching, customizable language picker UI, and runtime translation of non-resource text.

[![Min SDK](https://img.shields.io/badge/Min%20SDK-21-green.svg)](https://developer.android.com/about/versions/lollipop)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg)](https://developer.android.com/about/versions/14)

## Features

- 🌍 **Instant Locale Switching** - Switch app language instantly using Android's per-app locale system
- 🎨 **Customizable Language Picker** - Drop-in UI component with extensive XML and programmatic customization
- 🤖 **On-Device Translation** - ML Kit-powered translation for non-resource text (backend/user content)
- 💾 **Smart Caching** - LRU memory cache + DataStore persistence for translations
- 🔒 **Placeholder Protection** - Preserves %s, {name}, ${expr} placeholders during translation
- 📱 **Per-App Language Preferences** - Uses Android 13+ per-app language system with backward compatibility
- 🔌 **Pluggable Architecture** - Easy to swap translation providers (ML Kit, cloud APIs, custom)

## Modules

| Module | Description |
|--------|-------------|
| `:autolocalize-core` | Pure Kotlin core with interfaces and caching (no Android deps) |
| `:autolocalize-android` | Android library for locale switching and persistence |
| `:autolocalize-views` | Customizable language picker View component |
| `:autolocalize-mlkit` | ML Kit Translator implementation for on-device translation |

## Installation

### Step 1: Add JitPack Repository

Add JitPack to your project's `settings.gradle.kts` (or root `build.gradle.kts` if using older Gradle):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // Add JitPack
    }
}
```

**For older Gradle projects** (if `settings.gradle.kts` doesn't exist), add to root `build.gradle`:

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }  // Add JitPack
    }
}
```

### Step 2: Add Dependencies

Add the AutoLocalize modules to your app's `build.gradle.kts` (or `build.gradle`):
**Replace:**
- `VERSION` → Release version (e.g., `1.0.0` or `v1.0.0`)

```kotlin
dependencies {
    // Core + Android (required)
    implementation("com.github.Gloomy1233:autolocalize:VERSION:autolocalize-android")
    
    // Language picker View (optional)
    implementation("com.github.Gloomy1233:autolocalize:VERSION:autolocalize-views")
    
    // ML Kit translator for runtime translation (optional)
    implementation("com.github.Gloomy1233:autolocalize:VERSION:autolocalize-mlkit")
}
```


### Step 3: Sync and Build

1. Click **Sync Now** in Android Studio
2. Wait for dependencies to download
3. Build your project

### Module Dependencies

| Module | Artifact | Required | Description |
|--------|----------|----------|-------------|
| Core + Android | `autolocalize-android` | ✅ **Yes** | Main library with locale switching and persistence |
| Language Picker | `autolocalize-views` | ❌ Optional | Customizable language picker UI component |
| ML Kit Translator | `autolocalize-mlkit` | ❌ Optional | On-device translation using Google ML Kit |

**Note:** `autolocalize-android` automatically includes `autolocalize-core`. You don't need to add it separately.

### Getting the Latest Version

1. Visit: `https://jitpack.io/#YOUR_USERNAME/autolocalize`
2. Find the latest release version
3. Use that version in your dependencies

You can also use:
- `-SNAPSHOT` for the latest commit (e.g., `main-SNAPSHOT`)
- Commit hash for a specific commit (e.g., `abc123def`)
- Tag name for releases (e.g., `v1.0.0` or `1.0.0`)

## Quick Start

### 1. Initialize in Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        AutoLocalize.init(
            application = this,
            config = AutoLocalizeConfig.builder()
                .sourceLocaleTag("en")
                .supportedLocales("en", "es", "fr", "de", "ja", "zh")
                .translator(MlKitTranslator()) // Optional: for runtime translation
                .build()
        )
    }
}
```

### 2. Add Language Resources (strings.xml)

Create localized string resources as usual:

**res/values/strings.xml** (English - default)
```xml
<resources>
    <string name="app_name">My App</string>
    <string name="greeting">Hello, %1$s!</string>
</resources>
```

**res/values-es/strings.xml** (Spanish)
```xml
<resources>
    <string name="app_name">Mi Aplicación</string>
    <string name="greeting">¡Hola, %1$s!</string>
</resources>
```

### 3. Add Per-App Language Support (Android 13+)

Create `res/xml/locales_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
    <locale android:name="en" />
    <locale android:name="es" />
    <locale android:name="fr" />
    <locale android:name="de" />
</locale-config>
```

Reference it in `AndroidManifest.xml`:
```xml
<application
    android:localeConfig="@xml/locales_config"
    ...>
```

### 4. Add Language Picker to Layout

```xml
<com.autolocalize.views.AutoLocalizeLanguagePickerView
    android:id="@+id/languagePicker"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:al_languages="@array/supported_languages"
    app:al_hintText="Select Language"
    app:al_showLabelMode="nativeName"
    app:al_cornerRadius="8dp"
    app:al_strokeWidth="1dp" />
```

Define the languages array in `res/values/arrays.xml`:
```xml
<string-array name="supported_languages">
    <item>en</item>
    <item>es</item>
    <item>fr</item>
    <item>de</item>
</string-array>
```

### 5. Switch Locale Programmatically (Optional)

```kotlin
// Set locale
AutoLocalize.setLocale("es")

// Get current locale
val currentLocale = AutoLocalize.getLocale()
val currentTag = AutoLocalize.getLocaleTag() // "es"

// Observe locale changes
lifecycleScope.launch {
    AutoLocalize.observeLocale().collect { locale ->
        // Handle locale change
    }
}
```

## Runtime Translation (Non-Resource Text)

For translating backend content, user-generated text, or hardcoded strings:

```kotlin
// Suspend function
val translated = AutoLocalize.translate(
    text = "This is backend content",
    context = TranslationContext.BACKEND
)

// Async with callback
AutoLocalize.translateAsync("Hello from the server") { translatedText ->
    textView.text = translatedText
}
```

### Translation Contexts

| Context | Use Case |
|---------|----------|
| `TranslationContext.UI` | UI strings, labels |
| `TranslationContext.BACKEND` | API responses, server messages |
| `TranslationContext.USER_CONTENT` | User comments, posts |
| `TranslationContext.SYSTEM` | System notifications, errors |

## Language Picker Customization

### XML Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| `al_languages` | reference | String array of language tags |
| `al_languageLabels` | reference | Optional string array of display labels |
| `al_showLabelMode` | enum | `displayName`, `nativeName`, `customLabel` |
| `al_showFlag` | boolean | Show flag icons (future) |
| `al_textColor` | color | Text color |
| `al_hintText` | string | Hint text |
| `al_hintColor` | color | Hint text color |
| `al_dropdownBackgroundColor` | color | Dropdown background |
| `al_strokeColor` | color | Border color |
| `al_strokeWidth` | dimension | Border width |
| `al_cornerRadius` | dimension | Corner radius |
| `al_textSize` | dimension | Text size |
| `al_fontFamily` | string/reference | Font family |
| `al_iconTint` | color | Dropdown icon tint |
| `al_popupElevation` | dimension | Dropdown elevation |

### Programmatic Configuration

```kotlin
// Set languages programmatically
languagePicker.setLanguages(listOf(
    LanguageOption("en", "English", "English"),
    LanguageOption("es", "Spanish", "Español"),
    LanguageOption("fr", "French", "Français")
))

// Apply custom style
languagePicker.setStyle(PickerStyle.builder()
    .textColor(Color.BLACK)
    .cornerRadiusPx(24)
    .strokeWidthPx(2)
    .showLabelMode(ShowLabelMode.NATIVE_NAME)
    .build())

// Listen for selection changes
languagePicker.setOnLanguageChangedListener { option ->
    Log.d("Language", "Selected: ${option.tag}")
}
```

### Built-in Styles

```xml
<!-- Default outlined style -->
<com.autolocalize.views.AutoLocalizeLanguagePickerView
    style="@style/Widget.AutoLocalize.LanguagePicker" />

<!-- Outlined variant -->
<com.autolocalize.views.AutoLocalizeLanguagePickerView
    style="@style/Widget.AutoLocalize.LanguagePicker.Outlined" />

<!-- Filled variant -->
<com.autolocalize.views.AutoLocalizeLanguagePickerView
    style="@style/Widget.AutoLocalize.LanguagePicker.Filled" />

<!-- Compact for toolbars -->
<com.autolocalize.views.AutoLocalizeLanguagePickerView
    style="@style/Widget.AutoLocalize.LanguagePicker.Compact" />
```

## Opt-in View Tree Translation

Automatically translate hardcoded text in Views:

### Enable in Config

```kotlin
AutoLocalize.init(
    application = this,
    config = AutoLocalizeConfig.builder()
        .enableViewTreeTranslation(true)
        .viewTreeMode(TranslateMode.ONLY_TAGGED)
        .build()
)

// Install activity callbacks
AutoLocalizeViews.installActivityCallbacks(this)
```

### Tag Views for Translation

```xml
<!-- This will be translated -->
<TextView
    android:tag="al_translate"
    android:text="Translate this hardcoded text" />

<!-- This will NOT be translated -->
<TextView
    android:tag="al_no_translate"
    android:text="Keep this in original language" />
```

### Manual View Tree Translation

```kotlin
// Translate specific view tree
AutoLocalizeViews.translateViewTree(rootView, TranslateMode.ONLY_TAGGED)

// Revert to original text
AutoLocalizeViews.revertViewTree(rootView)
```

## ML Kit Translator

### Download Models

```kotlin
lifecycleScope.launch {
    // Check if ready
    val isReady = AutoLocalize.isTranslatorReady()
    
    // Download models if needed
    val result = AutoLocalize.prepareTranslator()
    when (result) {
        is PrepareResult.Ready -> { /* Ready to translate */ }
        is PrepareResult.Downloading -> { /* Show progress */ }
        is PrepareResult.Failed -> { /* Handle error */ }
    }
}
```

### Observe Download State

```kotlin
val mlKitTranslator = MlKitTranslator()

lifecycleScope.launch {
    mlKitTranslator.downloadState.collect { state ->
        when (state) {
            is MlKitTranslator.DownloadState.Idle -> { }
            is MlKitTranslator.DownloadState.Downloading -> {
                showProgress(state.progress)
            }
            is MlKitTranslator.DownloadState.Complete -> {
                hideProgress()
            }
            is MlKitTranslator.DownloadState.Failed -> {
                showError(state.error)
            }
        }
    }
}
```

### Manage Models

```kotlin
val translator = MlKitTranslator()

// Get downloaded models
val downloadedLanguages = translator.getDownloadedModels()

// Delete a model
translator.deleteModel("es")
```

## Custom Translator (Cloud API)

Implement your own translator:

```kotlin
class MyCloudTranslator(private val apiKey: String) : Translator {
    
    override suspend fun translate(
        text: String,
        sourceLanguageTag: String,
        targetLanguageTag: String,
        context: TranslationContext
    ): String {
        // Call your translation API
        return myApi.translate(text, sourceLanguageTag, targetLanguageTag)
    }
    
    override suspend fun isReady(
        sourceLanguageTag: String,
        targetLanguageTag: String
    ): Boolean = true
}

// Use it
AutoLocalize.init(
    application = this,
    config = AutoLocalizeConfig.builder()
        .translator(MyCloudTranslator("YOUR_API_KEY"))
        .build()
)
```

## Placeholder Protection

The library automatically protects placeholders during translation:

```kotlin
// Original: "Hello %1$s, you have %d messages"
// Translated to Spanish: "Hola %1$s, tienes %d mensajes"
// Placeholders preserved!

val text = "Welcome {user_name}! Balance: ${balance}"
val translated = AutoLocalize.translate(text)
// Placeholders {user_name} and ${balance} are preserved
```

Supported placeholder formats:
- Android format specifiers: `%s`, `%d`, `%1$s`, `%2$,d`
- Named placeholders: `{name}`, `{user_id}`
- Template literals: `${variable}`, `${expression}`
- HTML tags: `<b>`, `</b>`, `<br/>`

## Configuration Options

```kotlin
AutoLocalizeConfig.builder()
    // Source language for translation
    .sourceLocaleTag("en")
    
    // Supported locales for validation
    .supportedLocales("en", "es", "fr", "de")
    
    // Translator implementation
    .translator(MlKitTranslator())
    
    // Cache policy
    .cachePolicy(CachePolicy(
        maxMemoryEntries = 1000,
        persistToDisk = true,
        ttlMillis = -1 // Never expire
    ))
    
    // Persist selected locale
    .persistLocale(true)
    
    // Protect placeholders during translation
    .protectPlaceholders(true)
    
    // View tree translation
    .enableViewTreeTranslation(false)
    .viewTreeMode(TranslateMode.ONLY_TAGGED)
    
    .build()
```

## Troubleshooting

### Model Download Issues

**Problem:** ML Kit model download fails

**Solutions:**
1. Ensure internet permission is granted
2. Check Wi-Fi connectivity (default requires Wi-Fi)
3. Use custom download conditions:
   ```kotlin
   MlKitTranslator(
       downloadConditions = DownloadConditions.Builder()
           .requireWifi()
           .build()
   )
   ```

### Activity Recreation

**Problem:** Locale resets after configuration change

**Solutions:**
1. Ensure `persistLocale` is `true` (default)
2. Initialize AutoLocalize in `Application.onCreate()`
3. Use proper activity lifecycle handling

### Translations Not Updating

**Problem:** Strings don't change after locale switch

**Solutions:**
1. Use `AppCompatActivity` (required for per-app locale)
2. Ensure strings exist in target locale's `strings.xml`
3. Call `recreate()` on activities if needed

### Memory Usage

**Problem:** High memory usage from translation cache

**Solutions:**
1. Reduce memory cache size:
   ```kotlin
   .cachePolicy(CachePolicy(maxMemoryEntries = 500))
   ```
2. Clear cache periodically:
   ```kotlin
   AutoLocalize.clearCache()
   ```

## Limitations

1. **Resource-based localization** requires strings.xml files for each supported locale
2. **ML Kit translation** requires initial model download (~30MB per language)
3. **View tree translation** only works with TextView and its subclasses
4. **Per-app locale** requires AppCompatActivity and Android 13+ for system settings integration

## Requirements

- **Min SDK:** 21 (Android 5.0)
- **Target SDK:** 36
- **Kotlin:** 2.0+
- **AndroidX AppCompat:** 1.7+
- **Material Components:** 1.13+

## Publishing the Library

If you want to publish your own version of AutoLocalize, see [PUBLISHING.md](PUBLISHING.md) for detailed instructions on:

- Publishing to JitPack (easiest)
- Publishing to Maven Central (production-ready)
- Publishing to local Maven (for testing)

## License

```
Copyright 2024

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Sample App

The `:sample-app` module demonstrates all features of the library. Build and run it to see:

- Resource-based localization with language switching
- Runtime translation of backend content
- User input translation
- Opt-in view tree translation
- ML Kit model status and download

