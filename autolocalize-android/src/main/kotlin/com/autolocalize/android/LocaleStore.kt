package com.autolocalize.android

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore instance for locale preferences.
 */
private val Context.localeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "autolocalize_prefs"
)

/**
 * Handles persistence of locale selection using DataStore.
 */
internal class LocaleStore(private val context: Context) {
    
    companion object {
        private val KEY_LOCALE = stringPreferencesKey("selected_locale")
    }
    
    /**
     * Saves the selected locale tag.
     */
    suspend fun saveLocale(localeTag: String) {
        context.localeDataStore.edit { preferences ->
            preferences[KEY_LOCALE] = localeTag
        }
    }
    
    /**
     * Gets the saved locale tag, or null if none saved.
     */
    suspend fun getLocale(): String? {
        return context.localeDataStore.data.first()[KEY_LOCALE]
    }
    
    /**
     * Observes the saved locale tag.
     */
    fun observeLocale(): Flow<String?> {
        return context.localeDataStore.data.map { preferences ->
            preferences[KEY_LOCALE]
        }
    }
    
    /**
     * Clears the saved locale.
     */
    suspend fun clearLocale() {
        context.localeDataStore.edit { preferences ->
            preferences.remove(KEY_LOCALE)
        }
    }
}

