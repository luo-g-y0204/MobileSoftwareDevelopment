# Lab15：Flight Search 应用项目报告

## 1. Entity 设计说明

### Airport 实体
```kotlin
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val iata_code: String,    // 机场 IATA 代码（如 PEK、SHA）
    val name: String,        // 机场名称
    val passengers: Int       // 年客流量
)
```
**表映射**: `airport` 表包含机场的基本信息。

### Favorite 实体
```kotlin
@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val departure_code: String,   // 出发机场代码
    val destination_code: String  // 目的地机场代码
)
```
**表映射**: `favorite` 表记录用户收藏的航班信息。

---

## 2. DAO 查询方法设计说明

### FlightDao

| 方法 | 功能 | SQL 实现 |
|------|------|----------|
| `searchAirports(query)` | 自动补全搜索 | `SELECT * FROM airport WHERE iata_code LIKE :query OR name LIKE :query ORDER BY passengers DESC` |
| `getDestinations(departureCode)` | 获取目的地列表 | `SELECT * FROM airport WHERE iata_code != :departureCode ORDER BY name ASC` |
| `getAllAirports()` | 获取所有机场 | `SELECT * FROM airport ORDER BY name ASC` |
| `getAirportByCode(iataCode)` | 根据代码查询机场 | `SELECT * FROM airport WHERE iata_code = :iataCode` |

### FavoriteDao

| 方法 | 功能 | SQL 实现 |
|------|------|----------|
| `getAllFavorites()` | 获取所有收藏 | `SELECT * FROM favorite` |
| `getFavorite(departure, destination)` | 检查是否已收藏 | `SELECT * FROM favorite WHERE departure_code = :d AND destination_code = :dest` |
| `insertFavorite(favorite)` | 添加收藏 | INSERT 操作 |
| `deleteFavorite(departure, destination)` | 删除收藏 | `DELETE FROM favorite WHERE ...` |

---

## 3. LIKE 关键字的使用方法和作用

**LIKE 关键字**用于在 SQL 中进行模糊匹配查询。

### 使用方法
```sql
SELECT * FROM airport 
WHERE iata_code LIKE '%PE%' OR name LIKE '%北京%'
ORDER BY passengers DESC
```

### 通配符说明
- `%` : 匹配任意长度的任意字符（包括空字符）
- `_` : 匹配单个任意字符

### 作用
1. **自动补全功能**：用户输入部分字符时，匹配包含该字符的所有结果
2. **模糊搜索**：支持按机场代码或名称进行搜索
3. **提升用户体验**：无需输入完整内容即可找到目标
4. **按客流量排序**：搜索结果按 passengers 降序排列，优先显示热门机场

### 代码实现
```kotlin
@Query("SELECT * FROM airport WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery ORDER BY passengers DESC")
fun searchAirports(searchQuery: String): Flow<List<Airport>>
```

---

## 4. 联合查询的实现和作用

### 联合查询场景

**场景**：显示收藏航班的完整信息（出发机场名称 + 目的地机场名称）

### 实现方式

虽然本项目中使用了两个独立的查询，但可以通过以下方式实现联合查询：

```sql
SELECT f.departure_code, f.destination_code, 
       dep.name as departureName, dest.name as destinationName
FROM favorite f
INNER JOIN airport AS departure ON f.departure_code = departure.iata_code
INNER JOIN airport AS destination ON f.destination_code = destination.iata_code
```

### 作用
1. **数据关联**：将多个表的数据合并显示
2. **数据完整性**：获取完整的航班信息（代码 + 名称）
3. **性能优化**：减少多次数据库查询

---

## 5. Preferences DataStore 的使用场景和实现

### 使用场景

1. **保存搜索历史**：记录用户上次搜索的关键词
2. **保存用户偏好设置**：如选中的机场、主题设置等
3. **轻量级数据存储**：存储简单的键值对数据

### 实现代码

```kotlin
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

    suspend fun saveLastSearchQuery(query: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SEARCH_QUERY] = query
        }
    }
}
```

### 优势
- **异步操作**：不阻塞主线程
- **数据观察**：支持 Flow 实时监听数据变化
- **生命周期感知**：自动管理数据更新

---

## 6. ViewModel 状态管理设计

### UI 状态数据类

```kotlin
data class FlightUiState(
    val searchQuery: String = "",
    val searchResults: List<Airport> = emptyList(),
    val selectedAirport: Airport? = null,
    val destinations: List<Airport> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
    val isSearching: Boolean = false
)
```

### 状态管理流程

```
用户输入 → updateSearchQuery() → _searchQuery 变化 → searchResults 更新 → UI 刷新
选择机场 → selectAirport() → _selectedAirport 变化 → destinations 更新 → UI 刷新
收藏操作 → toggleFavorite() → favorites 变化 → UI 刷新
```

### 使用 Flow 组合多个数据流

```kotlin
val uiState: StateFlow<FlightUiState> = combine(
    _searchQuery,
    _selectedAirport,
    getAllFavorites()
) { query, selected, favorites ->
    FlightUiState(
        searchQuery = query,
        selectedAirport = selected,
        favorites = favorites
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = FlightUiState()
)
```

---

## 7. UI 切换逻辑说明

### 界面状态切换

| 条件 | 显示界面 |
|------|----------|
| `selectedAirport != null` | 目的地列表界面 |
| `searchResults.isNotEmpty()` | 搜索结果界面 |
| 其他 | 收藏列表界面（首页） |

### 代码实现

```kotlin
when {
    uiState.selectedAirport != null -> {
        DestinationsList(...)
    }
    searchResults.isNotEmpty() -> {
        SearchResultsList(...)
    }
    else -> {
        FavoritesList(...)
    }
}
```

### 导航逻辑

1. **返回首页**：点击返回按钮 → `clearSelection()` → 显示收藏列表
2. **选择机场**：点击搜索结果 → `selectAirport()` → 显示目的地列表
3. **搜索**：输入关键词 → `updateSearchQuery()` → 显示搜索结果

---

## 8. 实验中遇到的问题与解决过程

### 问题 1：数据库文件未正确加载

**现象**：应用启动时无法读取预填充的数据库

**原因**：`createFromAsset()` 路径不正确

**解决**：
1. 确保数据库文件位于 `assets/flight_search.db`
2. 检查 Room 版本是否支持 `createFromAsset()`
3. 在 Database 类中正确配置：

```kotlin
Room.databaseBuilder(...)
    .createFromAsset("flight_search.db")
    .build()
```

### 问题 2：Flow 数据流未正确收集

**现象**：UI 界面无法实时更新

**原因**：忘记使用 `collectAsState()` 收集数据流

**解决**：
```kotlin
val uiState by viewModel.uiState.collectAsState()
```

### 问题 3：异步操作未在后台线程执行

**现象**：数据库操作阻塞主线程，导致 ANR

**原因**：Room 挂起函数需要在 CoroutineScope 中调用

**解决**：
```kotlin
viewModelScope.launch(Dispatchers.IO) {
    favoriteDao.insertFavorite(favorite)
}
```

---

## 项目文件清单

| 文件 | 说明 |
|------|------|
| `Airport.kt` | 机场实体类 |
| `Favorite.kt` | 收藏实体类 |
| `FlightDao.kt` | 机场数据访问接口 |
| `FavoriteDao.kt` | 收藏数据访问接口 |
| `FlightDatabase.kt` | Room 数据库类 |
| `UserPreferencesRepository.kt` | DataStore 偏好设置 |
| `FlightViewModel.kt` | 视图模型 |
| `FlightScreen.kt` | Compose UI 界面 |
| `flight_search.db` | 预填充数据库 |
| `screenshot_search.png` | 搜索界面截图 |
| `screenshot_flights.png` | 航班列表截图 |
| `screenshot_favorites.png` | 收藏列表截图 |
| `report.md` | 实验报告 |