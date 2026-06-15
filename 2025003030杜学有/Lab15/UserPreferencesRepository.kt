package com.example.flightsearch.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private val SEARCH_TEXT = stringPreferencesKey("search_text")

    suspend fun saveSearchText(text: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_TEXT] = text
        }
    }

    val searchTextFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[SEARCH_TEXT] ?: ""
        }
}