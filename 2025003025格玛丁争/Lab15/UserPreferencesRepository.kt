package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.flightSearchDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "flight_search_preferences"
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private companion object {
        val SEARCH_TEXT = stringPreferencesKey("search_text")
    }

    val searchTextFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences[SEARCH_TEXT].orEmpty() }

    suspend fun saveSearchText(text: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_TEXT] = text
        }
    }
}
