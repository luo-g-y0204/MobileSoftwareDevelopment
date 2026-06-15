package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val SEARCH_TEXT = stringPreferencesKey("search_text")
    }

    val searchText: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SEARCH_TEXT] ?: ""
        }

    suspend fun saveSearchText(text: String) {
        context.dataStore.edit { preferences ->
            preferences[SEARCH_TEXT] = text
        }
    }
}