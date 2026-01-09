# AutoLocalize Sample App Guide

## Overview

The sample app demonstrates all features of the AutoLocalize library through multiple real-world use case scenarios. It includes a navigation drawer with 5 different demo screens, each showcasing different aspects of localization and translation.

## Demo Screens

### 1. 📚 Overview
**Purpose:** Basic features demonstration

**Features Shown:**
- Language picker with flags and native names
- Resource-based localization (`strings.xml`)
- Placeholder preservation (`%s`, `%d`, `{name}`)
- Runtime translation of backend content
- User input translation
- View tree translation with `al_translate` tags
- ML Kit model download status

**Use Cases:**
- Understanding basic setup
- Testing placeholder protection
- Learning view tree translation

### 2. 💬 Messaging App
**Purpose:** Real-time chat with automatic message translation

**Features Shown:**
- RecyclerView with message list
- Automatic translation of all messages when language changes
- New message translation on send
- Timestamp formatting
- User-generated content translation (`TranslationContext.USER_CONTENT`)

**Use Cases:**
- Chat applications
- Messaging platforms
- Social media apps
- Customer support chats

**How It Works:**
1. Messages are stored with original English text
2. When language changes, all messages are translated
3. New messages are translated immediately after sending
4. Original text is preserved for reverting

### 3. 🍕 Food Delivery
**Purpose:** Restaurant menu with translated items

**Features Shown:**
- RecyclerView with food items
- Translated product names and descriptions
- Price formatting (locale-aware)
- Rating display
- Backend content translation (`TranslationContext.BACKEND`)

**Use Cases:**
- Food delivery apps
- Restaurant menus
- E-commerce product listings
- Catalog applications

**How It Works:**
1. Menu items loaded with English names/descriptions
2. All items translated when language changes
3. Prices remain numeric (not translated)
4. Shows how to handle mixed content (text + numbers)

### 4. 📰 News Feed
**Purpose:** News articles with translated titles and content

**Features Shown:**
- RecyclerView with news articles
- Translated article titles
- Translated article content (preview)
- Date formatting (locale-aware)
- Backend API content translation

**Use Cases:**
- News applications
- Blog readers
- Content aggregators
- RSS feed readers

**How It Works:**
1. Articles loaded from "backend" (simulated)
2. Titles and content translated separately
3. Dates formatted according to locale
4. Demonstrates handling long-form content

### 5. 🛒 E-Commerce
**Purpose:** Product listings with translated information

**Features Shown:**
- RecyclerView with products
- Translated product names
- Translated product descriptions
- Review count formatting
- Price display
- Backend product data translation

**Use Cases:**
- E-commerce apps
- Shopping platforms
- Product catalogs
- Marketplace applications

**How It Works:**
1. Products loaded with English data
2. Names and descriptions translated
3. Prices and review counts remain numeric
4. Shows handling of product metadata

## Navigation

### Drawer Menu
Access via hamburger menu (☰) in the top-left corner:

- **Overview** - Basic features demo
- **Messaging App** - Chat interface demo
- **Food Delivery** - Menu items demo
- **News Feed** - Articles demo
- **E-Commerce** - Products demo
- **Settings** - (Placeholder for future settings)

### Language Picker
Each screen includes a language picker that:
- Shows current language with flag emoji
- Allows instant language switching
- Updates all content immediately
- Persists selection across app restarts

## Key Features Demonstrated

### 1. Resource Localization
- All UI strings come from `strings.xml`
- Automatically localized based on selected locale
- No code changes needed for new languages
- Placeholder strings work correctly

### 2. Runtime Translation
- Backend content translated on-the-fly
- User-generated content translated
- Cached translations for performance
- Graceful fallback to original text

### 3. RecyclerView Integration
- Shows how to translate RecyclerView items
- Efficient translation of list data
- Proper adapter updates
- Handles large lists gracefully

### 4. Multiple Translation Contexts
- `TranslationContext.UI` - Interface strings
- `TranslationContext.BACKEND` - API responses
- `TranslationContext.USER_CONTENT` - User messages
- `TranslationContext.SYSTEM` - System messages

### 5. View Tree Translation
- Automatic translation of tagged views
- Opt-in with `al_translate` tag
- Opt-out with `al_no_translate` tag
- Preserves original text for reverting

## Testing the Library

### Test Scenarios

1. **Language Switching**
   - Open any screen
   - Change language using picker
   - Observe all content updates instantly

2. **Resource Strings**
   - Go to Overview screen
   - Change language
   - See all `strings.xml` content change

3. **Runtime Translation**
   - Download ML Kit models first
   - Go to Messaging/Food/News/E-commerce
   - Change language
   - See backend content translate

4. **RecyclerView Translation**
   - Navigate to any RecyclerView screen
   - Change language
   - All list items translate automatically

5. **Placeholder Protection**
   - Go to Overview screen
   - Check placeholder examples
   - Change language
   - Verify placeholders preserved

6. **View Tree Translation**
   - Go to Overview screen
   - Find green box with `al_translate` tag
   - Change language
   - See it translate automatically

## Architecture

### Fragment-Based Navigation
- Each demo is a separate Fragment
- MainActivity handles navigation drawer
- Fragments are independent and reusable
- Easy to add new demo screens

### Data Models
Each fragment has its own data model:
- `Message` - For messaging app
- `FoodItem` - For food delivery
- `NewsArticle` - For news feed
- `Product` - For e-commerce

### Translation Flow
1. Data loaded with source language (English)
2. User changes language
3. Locale change observed
4. All data translated via `AutoLocalize.translate()`
5. UI updated with translated content

## Best Practices Demonstrated

1. **Lazy Translation** - Only translate when needed
2. **Caching** - Translations are cached automatically
3. **Error Handling** - Graceful fallback to original text
4. **Performance** - Efficient RecyclerView updates
5. **User Experience** - Instant language switching
6. **Code Organization** - Clean separation of concerns

## Extending the Sample

To add a new demo screen:

1. Create new Fragment class
2. Create layout XML
3. Add menu item in `nav_menu.xml`
4. Add string resources
5. Register in MainActivity navigation

Example:
```kotlin
// In MainActivity.kt
R.id.nav_new_demo -> loadFragment(NewDemoFragment())
```

## Troubleshooting

**Translations not working?**
- Make sure ML Kit models are downloaded
- Check if translator is ready: `AutoLocalize.isTranslatorReady()`
- Verify source locale matches your content language

**RecyclerView not updating?**
- Ensure adapter is notified: `adapter.notifyItemChanged(position)`
- Check if translation completed successfully
- Verify locale observer is active

**Language picker not showing all options?**
- This should be fixed with the custom filter
- Try clicking the dropdown again
- Check if languages array is properly loaded

