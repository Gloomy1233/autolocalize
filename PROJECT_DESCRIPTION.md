# AutoLocalize - Android Localization Library

## Project Description

**AutoLocalize** is a production-quality Android library that provides comprehensive app localization capabilities, enabling developers to implement multi-language support with both resource-based localization and runtime translation of dynamic content.

### Overview

AutoLocalize simplifies the process of creating multilingual Android applications by combining Android's native resource localization system with on-device machine learning translation. It offers a complete solution for apps that need to support multiple languages, including instant locale switching, customizable UI components, and automatic translation of user-generated and backend content.

### Key Features

#### 1. **Instant Locale Switching**
- Seamless language switching using Android's per-app locale system (Android 13+)
- Backward compatible with older Android versions
- Automatic persistence of user language preferences
- Observable locale changes via Kotlin Flow

#### 2. **Resource-Based Localization**
- Full support for Android's standard `strings.xml` resource localization
- Automatic locale detection and switching
- Placeholder preservation in format strings (`%s`, `%d`, `{name}`, `${variable}`)
- Zero-configuration setup for standard Android localization

#### 3. **Runtime Translation Engine**
- On-device translation using Google ML Kit (no internet required after model download)
- Pluggable translator architecture supporting custom translation providers
- Smart caching system (in-memory LRU + disk persistence)
- Automatic placeholder protection during translation
- Support for multiple translation contexts (UI, Backend, User Content, System)

#### 4. **Customizable Language Picker UI**
- Ready-to-use `AutoLocalizeLanguagePickerView` component
- Multiple display modes: name only, flag only, or flag + name
- Extensive XML and programmatic customization options
- Material Design 3 integration
- Built-in support for 20+ languages with flag emojis

#### 5. **View Tree Translation (Opt-in)**
- Automatic translation of hardcoded text in Views
- Tag-based system (`al_translate` / `al_no_translate`)
- Activity lifecycle integration
- Preserves original text for reverting

### Architecture

AutoLocalize is built as a modular Gradle multi-module project:

- **`:autolocalize-core`** - Pure Kotlin core library with translation interfaces, caching, and placeholder protection (no Android dependencies)
- **`:autolocalize-android`** - Android-specific implementation for locale switching, DataStore persistence, and Android integration
- **`:autolocalize-views`** - UI components including the customizable language picker View
- **`:autolocalize-mlkit`** - ML Kit Translator implementation for on-device translation
- **`:sample-app`** - Comprehensive demo application showcasing all features

### Use Cases

**Perfect for:**
- Apps with static content that needs traditional localization (`strings.xml`)
- Apps with dynamic content from APIs that needs runtime translation
- Apps with user-generated content (comments, posts) requiring translation
- Apps needing instant language switching without app restart
- Apps requiring offline translation capabilities
- Apps with mixed content (some localized, some translated)

**Ideal Scenarios:**
- E-commerce apps with product descriptions from backend
- Social media apps with user-generated content
- News apps with articles in multiple languages
- Business apps with dynamic notifications and messages
- Any app requiring flexible, multi-language support

### Technical Highlights

- **Min SDK:** 21 (Android 5.0 Lollipop)
- **Target SDK:** 36
- **Language:** 100% Kotlin
- **Coroutines:** Full coroutine support for async operations
- **Architecture:** Clean, modular design with clear separation of concerns
- **Thread Safety:** All caching and state management is thread-safe
- **Memory Efficient:** Configurable LRU cache with disk persistence
- **Error Handling:** Graceful degradation when translation fails
- **Testing:** Comprehensive unit tests for core functionality

### Quick Start

```kotlin
// 1. Initialize in Application
AutoLocalize.init(
    application = this,
    config = AutoLocalizeConfig.builder()
        .sourceLocaleTag("en")
        .supportedLocales("en", "es", "fr", "de")
        .translator(MlKitTranslator())
        .build()
)

// 2. Switch locale
AutoLocalize.setLocale("es")

// 3. Translate dynamic content
val translated = AutoLocalize.translate("Hello from backend", TranslationContext.BACKEND)
```

### Integration

Add to your `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.autolocalize:autolocalize-android:1.0.0")
    implementation("com.autolocalize:autolocalize-views:1.0.0")      // Optional
    implementation("com.autolocalize:autolocalize-mlkit:1.0.0")      // Optional
}
```

### Design Philosophy

AutoLocalize follows these core principles:

1. **Simplicity First** - Easy to integrate, minimal configuration required
2. **Flexibility** - Support both traditional localization and runtime translation
3. **Performance** - Efficient caching, lazy loading, minimal overhead
4. **Reliability** - Graceful error handling, fallback mechanisms
5. **Extensibility** - Pluggable architecture for custom translators
6. **Developer Experience** - Clear APIs, comprehensive documentation, sample app

### Comparison with Alternatives

**vs. Standard Android Localization:**
- ✅ Adds runtime translation for dynamic content
- ✅ Provides ready-made language picker UI
- ✅ Automatic placeholder protection
- ✅ Smart caching system

**vs. Cloud Translation APIs:**
- ✅ Works offline after initial model download
- ✅ No API keys or costs
- ✅ Better privacy (on-device processing)
- ✅ Faster response times

**vs. Other Libraries:**
- ✅ Combines resource localization + runtime translation
- ✅ Modern Kotlin-first API
- ✅ Modular architecture
- ✅ Production-ready with comprehensive testing

### Future Enhancements

- Cloud translator implementations (Google Cloud, DeepL, Azure)
- Additional UI components (language selection bottom sheet, dialog)
- Translation quality scoring
- Batch translation API
- Translation history and favorites
- Custom language model support

### License

Apache License 2.0 - See LICENSE file for details

### Contributing

Contributions are welcome! Please see CONTRIBUTING.md for guidelines.

---

**AutoLocalize** - Making Android app localization simple, flexible, and powerful.

