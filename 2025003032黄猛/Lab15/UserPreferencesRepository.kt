package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {
    private val searchTextKey = stringPreferencesKey("search_text")

    val searchTextFlow: Flow<String> = context.dataStore.data.map { preferences: Preferences ->
        preferences[searchTextKey] ?: ""
    }

    suspend fun saveSearchText(text: String) {
        context.dataStore.edit { preferences ->
            preferences[searchTextKey] = text
        }
    }
}