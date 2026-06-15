package com.example.flightsearch

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_pref")

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore
    private val SEARCH_TEXT_KEY = stringPreferencesKey("search_text")

    // 读取保存的搜索文本
    val searchTextFlow: Flow<String> = dataStore.data
        .map { it[SEARCH_TEXT_KEY] ?: "" }

    // 保存搜索文本
    suspend fun saveSearchText(text: String) {
        dataStore.edit { pref ->
            pref[SEARCH_TEXT_KEY] = text
        }
    }
}