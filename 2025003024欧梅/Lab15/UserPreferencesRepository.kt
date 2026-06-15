import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// DataStore 单例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("flight_prefs")
private val SEARCH_KEY = stringPreferencesKey("search_text")

class UserPreferencesRepository(private val ctx: Context) {
    // 读取搜索文本流
    val searchTextFlow: Flow<String> = ctx.dataStore.data
        .map { prefs -> prefs[SEARCH_KEY] ?: "" }

    // 保存搜索文本
    suspend fun saveSearch(text: String) {
        ctx.dataStore.edit { prefs ->
            prefs[SEARCH_KEY] = text
        }
    }

    // 同步读取（ViewModel初始化用）
    fun getSyncSearch(): String = runBlocking {
        searchTextFlow.first()
    }
}