package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// 全局DataStore实例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("flight_search_prefs")
// 存储搜索文本的键
private val SEARCH_TEXT_KEY = stringPreferencesKey("search_text")

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore

    // 读取持久化的搜索文本
    val savedSearchText: Flow<String> = dataStore.data
        .map { prefs -> prefs[SEARCH_TEXT_KEY] ?: "" }

    // 保存搜索文本到本地
    suspend fun saveSearchText(text: String) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs[SEARCH_TEXT_KEY] = text
        }
    }
}