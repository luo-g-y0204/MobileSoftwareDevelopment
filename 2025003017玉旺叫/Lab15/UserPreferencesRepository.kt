package com.example.flightsearch

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val LAST_SEARCH_QUERY = stringPreferencesKey("last_search_query")
        val SELECTED_AIRPORT = stringPreferencesKey("selected_airport")
    }

    val lastSearchQuery: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_SEARCH_QUERY] ?: ""
        }

    val selectedAirport: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_AIRPORT] ?: ""
        }

    suspend fun saveLastSearchQuery(query: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SEARCH_QUERY] = query
        }
    }

    suspend fun saveSelectedAirport(airportCode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_AIRPORT] = airportCode
        }
    }

    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}