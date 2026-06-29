package com.example.flightsearch

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore 扩展属性
 * 为 Context 创建单例 DataStore<Preferences> 实例
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flight_search_preferences")

/**
 * 用户偏好存储仓库
 * 使用 Preferences DataStore 持久化用户搜索文本
 * 在应用重启后恢复搜索状态
 */
class UserPreferencesRepository(private val context: Context) {

    companion object {
        /**
         * 搜索文本的偏好键
         */
        private val SEARCH_TEXT = stringPreferencesKey("search_text")

        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        /**
         * 获取单例实例
         */
        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferencesRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * 读取搜索文本 Flow
     * 从 DataStore 中持续读取保存的搜索文本
     * 如果不存在则返回空字符串
     */
    val searchTextFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SEARCH_TEXT] ?: ""
        }

    /**
     * 保存搜索文本
     * 将用户的搜索输入持久化到 DataStore 中
     * 需要在协程中调用
     *
     * @param text 要保存的搜索文本
     */
    suspend fun saveSearchText(text: String) {
        context.dataStore.edit { preferences ->
            preferences[SEARCH_TEXT] = text
        }
    }
}
