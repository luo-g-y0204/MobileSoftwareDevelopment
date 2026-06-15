package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 全局DataStore扩展
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")

class UserPreferencesRepository(private val context: Context) {
    private val SEARCH_TEXT_KEY = stringPreferencesKey("saved_search_text")

    // 读取保存的搜索文本流
    val savedSearchTextFlow: Flow<String> = context.dataStore.data
        .map { prefs ->
            prefs[SEARCH_TEXT_KEY] ?: ""
        }

    // 持久化保存搜索文字
    suspend fun saveSearchText(text: String) {
        context.dataStore.edit { prefs ->
            prefs[SEARCH_TEXT_KEY] = text
        }
    }
}