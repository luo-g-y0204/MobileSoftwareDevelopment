# Lab15：Flight Search 应用项目报告
## 1. Entity 实体设计（airport、favorite 数据表）
### Airport 机场实体
```kotlin
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val iata_code: String,
    val name: String,
    val passengers: Int
)
```
数据表作用：存储全部机场基础信息，按客流量排序展示热门机场，支撑搜索、航班目的地查询功能。

### Favorite 收藏实体
```kotlin
@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val departure_code: String,
    val destination_code: String
)
```
数据表作用：保存用户收藏的起终点机场组合，实现航班收藏、取消收藏、收藏列表查询功能。

---
## 2. DAO 数据查询接口设计
### 机场模糊搜索（搜索自动补全）
```kotlin
@Query("SELECT * FROM airport WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery ORDER BY passengers DESC")
fun searchAirports(searchQuery: String): Flow<List<Airport>>
```
功能：根据输入关键词模糊匹配机场代码/名称，优先展示客流量大的机场。

### 航班目的地查询
```kotlin
@Query("SELECT * FROM airport WHERE iata_code != :departureCode ORDER BY name ASC")
fun getDestinations(departureCode: String): Flow<List<Airport>>

@Query("SELECT * FROM airport WHERE iata_code = :iataCode")
suspend fun getAirportByCode(iataCode: String): Airport?
```
功能：选中出发机场后过滤所有不同目的地，支持通过机场代码快速查询单个机场信息。

### 收藏增删查操作
```kotlin
@Query("SELECT * FROM favorite")
fun getAllFavorites(): Flow<List<Favorite>>

@Query("SELECT * FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
suspend fun getFavorite(departureCode: String, destinationCode: String): Favorite?

@Insert
suspend fun insertFavorite(favorite: Favorite)

@Query("DELETE FROM favorite WHERE departure_code = :departureCode AND destinationCode = :destinationCode")
suspend fun deleteFavorite(departureCode: String, destinationCode: String)
```
功能：完整实现收藏添加、删除、批量查询、单条收藏判断逻辑。

---
## 3. SQL LIKE 模糊匹配
### 使用方式
查询语句内使用 `LIKE`，调用时拼接前后通配符 `%`：
```kotlin
val keyword = "%$userInput%"
searchAirports(keyword)
```
### 作用
无需输入完整机场名称/代码即可检索匹配内容，优化搜索交互体验。

---
## 4. 多数据流联合管理
使用 `combine` 合并多条 Flow，统一封装页面状态：
```kotlin
val uiState: StateFlow<FlightUiState> = combine(
    _searchQuery, _selectedAirport, getAllFavorites()
) { query, airport, favList ->
    FlightUiState(query, airport, favList)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FlightUiState())
```
作用：将搜索词、选中机场、收藏列表整合为单一UI状态，任意数据变更页面同步刷新。

---
## 5. DataStore 本地偏好存储
### 适用场景
替代 SharedPreferences，持久化轻量用户设置：上次搜索关键词、选中的出发机场，重启应用自动恢复页面状态。
### 核心代码
```kotlin
class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore
    private val LAST_SEARCH = stringPreferencesKey("last_search")
    private val SELECTED_AIRPORT = stringPreferencesKey("selected_airport")

    val lastSearch: Flow<String> = dataStore.data.map { it[LAST_SEARCH] ?: "" }
    val selectAirportCode: Flow<String> = dataStore.data.map { it[SELECTED_AIRPORT] ?: "" }

    suspend fun saveSearch(text: String) = dataStore.edit { it[LAST_SEARCH] = text }
    suspend fun saveAirportCode(code: String) = dataStore.edit { it[SELECTED_AIRPORT] = code }
}
```
优势：基于协程Flow实现，无主线程阻塞问题，线程安全。

---
## 6. ViewModel 状态管理
### 统一页面状态类
```kotlin
data class FlightUiState(
    val searchQuery: String = "",
    val selectedAirport: Airport? = null,
    val favorites: List<Favorite> = emptyList()
)
```
### ViewModel 业务逻辑
内部通过 MutableStateFlow 保存可变数据，对外暴露只读 uiState；封装搜索切换、机场选中、收藏切换、本地存储持久化全部逻辑，IO操作使用viewModelScope.launch(Dispatchers.IO)异步执行，避免阻塞主线程。

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
作用：自定义 Factory 用于给ViewModel注入Dao、DataStore仓库依赖，解决带参ViewModel无法直接实例化的问题。

---
## 7. Compose UI 页面切换逻辑
页面通过 uiState 状态判断渲染不同模块：
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
1. 已选中出发机场：展示目的地列表，支持收藏切换
2. 存在搜索结果：展示机场搜索列表，点击切换出发机场
3. 无搜索、无选中机场：展示用户收藏航班列表
状态变更自动触发Compose重组，无需手动刷新页面。

---
## 8. 实验问题与解决方案
### 问题1：Gradle爆红、项目同步失败
原因：依赖版本配置错误、网络拉取依赖缓慢、缓存损坏。
解决：更换阿里云Maven镜像，清理缓存重启Android Studio，手动指定稳定版Room、DataStore依赖。
```kotlin
dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

### 问题2：Room编译闪退，找不到Dao实现类
原因：未添加kapt注解处理器，Room无法自动生成数据库Dao实现代码。
解决：模块gradle添加插件，Clean&Rebuild项目重新生成代码。
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}
```

### 问题3：数据更新但界面不刷新
原因：Compose未订阅Flow状态变化。
解决：UI层使用collectAsState()接收ViewModel的StateFlow，数据变更自动重组页面。
```kotlin
@Composable
fun FlightScreen(viewModel: FlightViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val destinations by viewModel.destinations.collectAsState()
}
```

---
## 项目文件清单说明
Airport.kt 和 Favorite.kt：存放Room数据库实体类，对应两张数据表。
FlightDao.kt：数据库数据访问接口，封装全部查询、增删逻辑。
FlightDatabase.kt：Room数据库主类，定义数据库实例。
UserPreferencesRepository.kt：封装DataStore本地持久化存储工具。
FlightViewModel.kt 与对应的Factory文件：负责页面状态管理与所有业务逻辑。
FlightScreen.kt：Compose编写的主页面UI代码。
MainActivity.kt：应用程序启动入口。
report.md：本次Lab15实验报告文档。