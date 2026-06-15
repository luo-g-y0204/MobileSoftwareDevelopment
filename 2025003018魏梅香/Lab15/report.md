# Lab15：Flight Search 应用项目报告

## 1. Entity 设计说明（airport 和 favorite 表映射）

### Airport 实体

```kotlin
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val iata_code: String,    // 机场 IATA 代码（如 PEK、SHA）
    val name: String,         // 机场名称
    val passengers: Int       // 年客流量
)
```

**表映射**：`airport` 表存储所有机场的基础信息，支持按客流量降序排序，优先展示热门机场，用于搜索和航班查询。

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

**表映射**：`favorite` 表存储用户收藏的航班组合（出发 + 到达机场对），用于记录和查询用户的收藏状态。

---

## 2. DAO 查询方法设计说明（自动补全、航班查询、收藏查询）

### 自动补全（机场搜索）

```kotlin
@Query("SELECT * FROM airport WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery ORDER BY passengers DESC")
fun searchAirports(searchQuery: String): Flow<List<Airport>>
```

**说明**：根据用户输入的关键词，模糊匹配机场的 IATA 代码或名称，按客流量降序返回结果，实现搜索自动补全。

### 航班查询

```kotlin
@Query("SELECT * FROM airport WHERE iata_code != :departureCode ORDER BY name ASC")
fun getDestinations(departureCode: String): Flow<List<Airport>>

@Query("SELECT * FROM airport ORDER BY name ASC")
fun getAllAirports(): Flow<List<Airport>>

@Query("SELECT * FROM airport WHERE iata_code = :iataCode")
suspend fun getAirportByCode(iataCode: String): Airport?
```

**说明**：支持用户选中出发机场后查询所有可前往的目的地机场；同时支持通过 IATA 代码快速定位单个机场。

### 收藏查询与操作

```kotlin
@Query("SELECT * FROM favorite")
fun getAllFavorites(): Flow<List<Favorite>>

@Query("SELECT * FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
suspend fun getFavorite(departureCode: String, destinationCode: String): Favorite?

@Insert
suspend fun insertFavorite(favorite: Favorite)

@Query("DELETE FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
suspend fun deleteFavorite(departureCode: String, destinationCode: String)
```

**说明**：实现收藏的增、删、查功能，支持批量查询所有收藏，以及判断单个航班是否已被收藏。

---

## 3. LIKE 关键字的使用方法和作用

### 使用方法

在 DAO 的搜索方法中，通过 `LIKE` 关键字实现模糊匹配：

```kotlin
@Query("SELECT * FROM airport WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery ORDER BY passengers DESC")
fun searchAirports(searchQuery: String): Flow<List<Airport>>
```

调用时，为 `searchQuery` 参数拼接通配符 `%`，实现包含匹配：

```kotlin
val query = "%${userInput}%"
```

### 作用

**说明**：打破精确匹配限制，实现用户无需输入完整的机场代码或名称，即可找到相关结果，提升搜索体验（如输入 "pek" 可匹配 "PEK 北京首都国际机场"）。

---

## 4. 联合查询的实现和作用

### 实现方式

通过 Kotlin Flow 的 `combine` 方法，将多个独立数据流合并为统一的 UI 状态流：

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

### 作用

**说明**：将搜索词、选中机场、收藏列表等分散状态合并为统一的 `FlightUiState`，确保 UI 能同时感知所有数据变化，实现状态同步更新。

---

## 5. Preferences DataStore 的使用场景和实现

### 使用场景

用于存储用户的本地偏好数据，替代 SharedPreferences：
- 记录用户最后一次输入的搜索关键词
- 记录用户选中的出发机场，应用重启后恢复状态

### 实现方式

```kotlin
class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val LAST_SEARCH_QUERY = stringPreferencesKey("last_search_query")
        val SELECTED_AIRPORT = stringPreferencesKey("selected_airport")
    }

    val lastSearchQuery: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_SEARCH_QUERY] ?: "" }

    val selectedAirport: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SELECTED_AIRPORT] ?: "" }

    suspend fun saveLastSearchQuery(query: String) {
        dataStore.edit { it[PreferencesKeys.LAST_SEARCH_QUERY] = query }
    }

    suspend fun saveSelectedAirport(airportCode: String) {
        dataStore.edit { it[PreferencesKeys.SELECTED_AIRPORT] = airportCode }
    }
}
```

**说明**：DataStore 基于协程和 Flow 实现，线程安全，避免 SharedPreferences 的主线程阻塞问题，适合存储轻量级用户偏好。

---

## 6. ViewModel 状态管理设计

### 状态封装

```kotlin
data class FlightUiState(
    val searchQuery: String = "",
    val selectedAirport: Airport? = null,
    val favorites: List<Favorite> = emptyList()
)
```

### 状态管理实现

```kotlin
class FlightViewModel(
    private val flightDao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedAirport = MutableStateFlow<Airport?>(null)

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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _selectedAirport.value = null
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.saveLastSearchQuery(query)
        }
    }

    fun selectAirport(airport: Airport) {
        _selectedAirport.value = airport
        _searchQuery.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.saveSelectedAirport(airport.iata_code)
        }
    }

    fun clearSelection() {
        _selectedAirport.value = null
    }

    fun toggleFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = flightDao.getFavorite(departureCode, destinationCode)
            if (existing != null) {
                flightDao.deleteFavorite(departureCode, destinationCode)
            } else {
                flightDao.insertFavorite(
                    Favorite(departure_code = departureCode, destination_code = destinationCode)
                )
            }
        }
    }
}
```

**说明**：ViewModel 封装所有业务逻辑，通过 `StateFlow` 暴露只读状态，确保 UI 层仅观察状态、不直接修改数据。

### ViewModel 工厂

```kotlin
class FlightViewModelFactory(
    private val flightDao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlightViewModel::class.java)) {
            return FlightViewModel(flightDao, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

**说明**：由于 `FlightViewModel` 的构造函数需要 `FlightDao` 和 `UserPreferencesRepository` 参数，实现 `ViewModelProvider.Factory` 通过工厂模式注入依赖。

---

## 7. UI 切换逻辑说明

### 状态分支渲染

UI 根据 `FlightUiState` 的不同状态，切换不同的界面分支：

```kotlin
when {
    uiState.selectedAirport != null -> {
        DestinationsList(
            departureAirport = uiState.selectedAirport!!,
            destinations = destinations,
            favorites = uiState.favorites,
            onToggleFavorite = { destCode ->
                viewModel.toggleFavorite(uiState.selectedAirport!!.iata_code, destCode)
            }
        )
    }
    searchResults.isNotEmpty() -> {
        SearchResultsList(
            results = searchResults,
            onSelectAirport = { viewModel.selectAirport(it) }
        )
    }
    else -> {
        FavoritesList(
            favorites = uiState.favorites,
            onSelectFavorite = { depCode, destCode ->
                viewModel.updateSearchQuery(depCode)
            },
            onDeleteFavorite = { viewModel.deleteFavorite(it) }
        )
    }
}
```

**说明**：通过 `when` 分支判断 UI 状态，实现搜索页、目的地列表页、收藏列表页的切换，状态变更时自动触发 UI 刷新。

---

## 8. 实验中遇到的问题与解决过程

### 问题 1：Gradle 文件全爆红、同步失败

**现象**：打开项目后 Gradle 文件全爆红，同步失败，无法构建。

**原因**：`libs.versions.toml` 配置错误、网络下载慢、缓存损坏。

**解决**：
1. 重建纯净项目，使用直接依赖写法替代版本目录
2. 在 `settings.gradle.kts` 中配置国内镜像（阿里云 Maven）
3. 执行 `File → Invalidate Caches / Restart` 清理缓存

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

### 问题 2：Room 无法生成实现类导致应用闪退

**现象**：应用启动即闪退，日志报错找不到 `FlightDao_Impl` 类。

**原因**：注解处理器（kapt）依赖缺失或配置错误，Room 无法在编译时生成 DAO 的实现类。

**解决**：
1. 在模块级 `build.gradle.kts` 中正确添加 `room-compiler`：

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
}
```

2. 执行 `Build → Clean Project` 后 `Rebuild Project`，确保注解处理器重新运行生成代码。

### 问题 3：ViewModel 状态变化但 UI 不刷新

**现象**：点击收藏按钮后，心形图标状态没有变化；搜索输入后列表不更新。

**原因**：未使用 Compose 状态收集方法，UI 没有订阅 StateFlow 的变化。

**解决**：在 Compose 中使用 `collectAsState()` 收集 ViewModel 暴露的 StateFlow：

```kotlin
@Composable
fun FlightScreen(viewModel: FlightViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val destinations by viewModel.destinations.collectAsState()
    // ... UI 使用这些状态自动刷新
}
```

**说明**：`collectAsState()` 会将 Flow/StateFlow 转换为 Compose 可观察的状态，数据变化时自动触发重组（Recomposition），确保界面响应式更新。

---

## 项目文件清单

| 文件 | 说明 |
|------|------|
| `Airport.kt` | 机场实体类 |
| `Favorite.kt` | 收藏实体类 |
| `FlightDao.kt` | 数据访问接口 |
| `FlightDatabase.kt` | Room 数据库类 |
| `UserPreferencesRepository.kt` | DataStore 偏好设置 |
| `FlightViewModel.kt` | 视图模型 + 工厂 |
| `FlightScreen.kt` | Compose UI 界面 |
| `MainActivity.kt` | 应用入口 |
| `report.md` | 实验报告 |