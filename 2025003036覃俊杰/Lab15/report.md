# Lab15: Flight Search 应用实验报告

## 1. Entity 设计说明

### Airport 实体

`Airport` 实体映射数据库中的 `airport` 表，用于存储机场信息：

- **`id`** (INTEGER, 主键): 机场的唯一标识符，自增主键
- **`iata_code`** (VARCHAR): 机场的 IATA 三字母代码，如 "PEK"、"LAX"
- **`name`** (VARCHAR): 机场的全称，如 "Beijing Capital International Airport"
- **`passengers`** (INTEGER): 该机场的年客流量，用于排序优先显示热门机场

```kotlin
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "iata_code") val iataCode: String,
    val name: String,
    val passengers: Int
)
```

### Favorite 实体

`Favorite` 实体映射数据库中的 `favorite` 表，用于存储用户收藏的航线：

- **`id`** (INTEGER, 主键): 收藏记录的唯一标识符
- **`departure_code`** (VARCHAR): 出发地的 IATA 代码
- **`destination_code`** (VARCHAR): 目的地的 IATA 代码

```kotlin
@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "departure_code") val departureCode: String,
    @ColumnInfo(name = "destination_code") val destinationCode: String
)
```

## 2. DAO 查询方法设计说明

### 自动补全查询

```kotlin
@Query("""
    SELECT * FROM airport
    WHERE iata_code LIKE :query OR name LIKE :query
    ORDER BY passengers DESC
""")
fun searchAirports(query: String): Flow<List<Airport>>
```

- 使用 `LIKE` 关键字对 `iata_code` 和 `name` 两列进行模糊匹配
- 按 `passengers` 降序排列，确保客流量大的热门机场优先显示
- 调用时传入 `"%$searchQuery%"` 格式的参数实现部分匹配

### 航班查询

```kotlin
@Query("""
    SELECT * FROM airport
    WHERE iata_code != :departureCode
    ORDER BY passengers DESC
""")
fun getAvailableDestinations(departureCode: String): Flow<List<Airport>>
```

- 假设每个机场都有飞往其他所有机场的航班
- 使用 `!=` 排除出发地自身
- 按客流量降序排列

### 收藏航线查询（联合查询）

```kotlin
@Query("""
    SELECT 
        favorite.id AS id,
        favorite.departure_code AS departureCode,
        favorite.destination_code AS destinationCode,
        departure.name AS departureName,
        destination.name AS destinationName
    FROM favorite
    INNER JOIN airport AS departure ON favorite.departure_code = departure.iata_code
    INNER JOIN airport AS destination ON favorite.destination_code = destination.iata_code
""")
fun getAllFavorites(): Flow<List<FavoriteWithNames>>

data class FavoriteWithNames(
    val id: Int,
    val departureCode: String,
    val destinationCode: String,
    val departureName: String,
    val destinationName: String
)
```

### 收藏的增删操作

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun addFavorite(favorite: Favorite)

@Delete
suspend fun removeFavorite(favorite: Favorite)

@Query("SELECT * FROM favorite WHERE departure_code = :departure AND destination_code = :destination")
suspend fun getFavorite(departure: String, destination: String): Favorite?
```

- `addFavorite` 使用 `REPLACE` 策略避免重复插入
- `removeFavorite` 根据实体对象删除
- `getFavorite` 用于检查某条航线是否已收藏，以控制按钮状态

## 3. LIKE 关键字的使用方法和作用

`LIKE` 是 SQL 中用于模式匹配的关键字，在本应用中用于实现自动补全搜索。

**使用方法：**

```kotlin
@Query("""
    SELECT * FROM airport
    WHERE iata_code LIKE :query OR name LIKE :query
    ORDER BY passengers DESC
""")
fun searchAirports(query: String): Flow<List<Airport>>
```

在调用时，需要在搜索词前后添加 `%` 通配符：

```kotlin
val searchQuery = "%$userInput%"
dao.searchAirports(searchQuery)
```

**作用：**

- `%` 匹配零个或多个任意字符，实现"包含"语义的模糊搜索
- 例如输入 "BE" 会匹配 iata_code 为 "PEK"、"BEG" 的记录，也会匹配 name 中包含 "BE" 的机场
- 结合 `passengers DESC` 排序，优先显示热门机场，提升用户体验
- 用户无需输入完整名称即可找到目标机场

## 4. 联合查询的实现和作用

**实现方式：**

```kotlin
@Query("""
    SELECT 
        favorite.id AS id,
        favorite.departure_code AS departureCode,
        favorite.destination_code AS destinationCode,
        departure.name AS departureName,
        destination.name AS destinationName
    FROM favorite
    INNER JOIN airport AS departure ON favorite.departure_code = departure.iata_code
    INNER JOIN airport AS destination ON favorite.destination_code = destination.iata_code
""")
fun getAllFavorites(): Flow<List<FavoriteWithNames>>
```

**作用：**

- `favorite` 表只存储 IATA 代码，不包含机场名称
- 通过 `INNER JOIN` 将 `favorite` 表与 `airport` 表进行两次关联查询：
  - 第一次 JOIN：将 `departure_code` 关联到机场表的 `iata_code`，获取出发地名称
  - 第二次 JOIN：将 `destination_code` 关联到机场表的 `iata_code`，获取目的地名称
- 使用别名 `departure` 和 `destination` 区分两次关联
- 结果通过 `FavoriteWithNames` 数据类封装，包含代码和名称的完整信息

## 5. Preferences DataStore 的使用场景和实现

**使用场景：**

保存用户的搜索文本状态，实现应用重启后搜索文本的持久化恢复，提升用户体验。

**实现方式：**

```kotlin
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {
    
    companion object {
        private val SEARCH_TEXT = stringPreferencesKey("search_text")
    }

    val searchTextFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[SEARCH_TEXT] ?: ""
        }

    suspend fun saveSearchText(text: String) {
        context.dataStore.edit { preferences ->
            preferences[SEARCH_TEXT] = text
        }
    }
}
```

**关键点：**

- `stringPreferencesKey` 定义存储键
- `dataStore.data` 返回 `Flow`，可被 Compose 观察
- 使用 `catch` 处理 `IOException`（文件读写异常）
- 空字符串表示无搜索文本，此时显示收藏列表
- `edit` 方法在协程中执行原子性读写操作

## 6. ViewModel 状态管理设计

```kotlin
class FlightViewModel(
    private val dao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // 搜索文本状态
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // 自动补全建议
    val suggestions: StateFlow<List<Airport>> = _searchText
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else dao.searchAirports("%$query%")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 选定出发地
    private val _selectedAirport = MutableStateFlow<Airport?>(null)
    val selectedAirport: StateFlow<Airport?> = _selectedAirport.asStateFlow()

    // 航班列表
    val availableFlights: StateFlow<List<Airport>> = _selectedAirport
        .filterNotNull()
        .flatMapLatest { dao.getAvailableDestinations(it.iataCode) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 收藏列表
    val favorites: StateFlow<List<FavoriteWithNames>> = dao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 启动时从 DataStore 恢复搜索文本
        viewModelScope.launch {
            userPreferencesRepository.searchTextFlow.collect { text ->
                _searchText.value = text
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
        viewModelScope.launch {
            userPreferencesRepository.saveSearchText(text)
        }
    }

    fun onAirportSelected(airport: Airport) {
        _selectedAirport.value = airport
    }

    fun toggleFavorite(favorite: Favorite) {
        viewModelScope.launch {
            val existing = dao.getFavorite(favorite.departureCode, favorite.destinationCode)
            if (existing != null) dao.removeFavorite(existing)
            else dao.addFavorite(favorite)
        }
    }
}
```

**设计要点：**

- 使用 `StateFlow` 管理 UI 状态，确保 Compose 可观察
- `debounce(300)` 防止用户快速输入时频繁查询数据库
- `flatMapLatest` 确保每次新查询会取消上一次的查询协程
- `stateIn` 将 Flow 转换为 StateFlow，并指定共享策略
- 搜索文本的每次变化同步持久化到 DataStore
- `toggleFavorite` 根据当前收藏状态执行添加或删除

## 7. UI 切换逻辑说明

```kotlin
@Composable
fun FlightScreen(viewModel: FlightViewModel) {
    val searchText by viewModel.searchText.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val selectedAirport by viewModel.selectedAirport.collectAsState()
    val flights by viewModel.availableFlights.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Column {
        OutlinedTextField(
            value = searchText,
            onValueChange = { viewModel.onSearchTextChanged(it) },
            placeholder = { Text("输入机场名称或IATA代码") },
            modifier = Modifier.fillMaxWidth()
        )

        when {
            searchText.isNotBlank() && suggestions.isNotEmpty() -> {
                SuggestionList(suggestions) { airport ->
                    viewModel.onAirportSelected(airport)
                }
            }
            selectedAirport != null -> {
                FlightList(
                    departureCode = selectedAirport!!.iataCode,
                    flights = flights,
                    favorites = favorites,
                    onToggleFavorite = { destCode ->
                        val fav = Favorite(
                            id = 0,
                            departureCode = selectedAirport!!.iataCode,
                            destinationCode = destCode
                        )
                        viewModel.toggleFavorite(fav)
                    }
                )
            }
            searchText.isBlank() -> {
                FavoriteList(favorites = favorites)
            }
        }
    }
}
```

**切换逻辑总结：**

| 条件 | 显示内容 |
|------|---------|
| 搜索文本非空 + 有匹配建议 | 自动补全建议列表 |
| 已选定出发机场 | 航班列表（含收藏按钮） |
| 搜索文本为空 | 收藏航线列表 |

**关键设计：**

- 使用 `when` 表达式实现清晰的界面状态分支
- 建议列表优先于航班列表显示——用户选择建议后才切换到航班视图
- 搜索文本清空时自动回退到收藏列表视图
- 收藏按钮根据 `favorites` 中是否包含当前航线切换图标样式

## 8. 实验中遇到的问题与解决过程

### 问题一：Room 预填充数据库路径问题

**现象：** 数据库文件 `flight_search.db` 放置在 `assets/database/` 目录下，但应用启动时数据库未正确加载。

**解决：** 在 `FlightDatabase` 中使用 `createFromAsset("database/flight_search.db")` 指定正确的 assets 子目录路径。注意 Room 的 `createFromAsset` 路径相对于 `assets/` 目录，不需要前导斜杠。

### 问题二：LIKE 查询不区分大小写

**现象：** 用户输入小写字母时无法匹配数据库中大写存储的 IATA 代码。

**解决：** SQLite 的 `LIKE` 运算符默认对 ASCII 字符不区分大小写，但为确保兼容性，可以在查询时使用 `COLLATE NOCASE` 或统一在应用层将搜索词转为大写。最终采用在 DAO 调用层调用 `uppercase()` 方法处理。

### 问题三：收藏按钮状态闪烁

**现象：** 切换收藏时按钮图标闪烁，状态更新不及时。

**解决：** 使用 `getFavorite()` 结合 `Flow` 观察收藏状态，而非仅在点击时查询一次。将收藏检测逻辑改为 `StateFlow` 驱动，确保 Compose 重组时状态一致。

### 问题四：DataStore 读写协程作用域

**现象：** 在非协程环境中调用 DataStore 的 `edit` 方法导致崩溃。

**解决：** 在 ViewModel 中使用 `viewModelScope.launch` 启动协程执行 DataStore 读写操作，确保所有 DataStore 操作在协程上下文中执行。

### 问题五：屏幕旋转后状态丢失

**现象：** 旋转屏幕后已选择的机场和航班列表丢失。

**解决：** 使用 `StateFlow` 在 ViewModel 中管理状态，ViewModel 在屏幕旋转时不会重建（除非主动销毁），因此状态得以保持。同时利用 `SavedStateHandle` 进一步增强配置变更时的状态保持能力。
