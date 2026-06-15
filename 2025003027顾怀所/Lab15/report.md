# 航班搜索应用开发报告

## 1. Entity 设计说明（airport 和 favorite 表映射）

### 1.1 Airport 实体

`Airport` 类通过 Room 的 `@Entity` 注解映射数据库中的 `airport` 表，核心字段设计如下：

```kotlin
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,          // 自增主键，适配Room数据库规范
    val iata_code: String,    // 机场IATA编码（核心查询字段）
    val name: String,         // 机场名称
    val passengers: Int       // 客流量（用于搜索结果排序）
)
```

- 映射规则：字段名与预置数据库表 `airport` 的列名完全一致，避免字段不匹配导致的查询异常；

- 主键设计：使用自增主键 `id`，兼容 Room 的 CRUD 操作规范，同时保留 `iata_code` 作为业务唯一标识。

### 1.2 Favorite 实体

`Favorite` 类映射 `favorite` 表，用于存储用户收藏的航线，核心设计如下：

```kotlin
@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                  // 自增主键，用于删除操作
    val departure_code: String,       // 出发机场IATA编码
    val destination_code: String      // 到达机场IATA编码
    // 移除了departureName/destinationName字段，适配预置数据库表结构
)
```

- 核心优化：删除冗余的名称字段，解决因预置数据库无对应列导致的崩溃问题；

- 业务关联：通过 `departure_code` \+ `destination_code` 组合唯一标识一条收藏航线，支持收藏 / 取消收藏的精准判断。

## 2. DAO 查询方法设计说明

`FlightDao` 基于 Room 的 `@Dao` 注解设计，封装所有数据库查询逻辑，核心方法分类如下：

### 2.1 自动补全查询（搜索机场）

```kotlin
@Query("SELECT * FROM airport WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery ORDER BY passengers DESC")
fun searchAirports(searchQuery: String): Flow<List<Airport>>

@Query("SELECT * FROM airport ORDER BY name ASC")
fun getAllAirports(): Flow<List<Airport>>
```

- 核心逻辑：通过 `iata_code` 或 `name` 匹配搜索关键词，按客流量降序排序，优先展示热门机场；

- 扩展设计：`getAllAirports` 提供全量机场数据，用于 ViewModel 中组合筛选（如排除已选机场、匹配收藏机场名称）；

- 响应式设计：返回 `Flow` 类型，支持数据实时更新，UI 自动同步。

### 2.2 航班查询（目的地筛选）

```kotlin
@Query("SELECT * FROM airport WHERE iata_code != :departureCode ORDER BY name ASC")
fun getDestinations(departureCode: String): Flow<List<Airport>>
```

- 核心逻辑：排除当前选中的出发机场（`iata_code != departureCode`），返回所有可到达的机场；

- 排序规则：按机场名称升序排列，提升用户浏览体验；

- 场景适配：配合 ViewModel 的 `destinations` 流，实时更新出发机场对应的目的地列表。

### 2.3 收藏查询与操作

```kotlin
// 全量收藏列表（实时监听）
@Query("SELECT * FROM favorite")
fun getAllFavorites(): Flow<List<Favorite>>

// 精准查询单条收藏记录（判断是否已收藏）
@Query("SELECT * FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode LIMIT 1")
suspend fun getFavorite(departureCode: String, destinationCode: String): Favorite?

// 收藏操作
@Insert
suspend fun insertFavorite(favorite: Favorite)

// 取消收藏（两种方式：按编码/按主键）
@Query("DELETE FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
suspend fun deleteFavorite(departureCode: String, destinationCode: String)

@Query("DELETE FROM favorite WHERE id = :id")
suspend fun deleteFavoriteById(id: Int)
```

- 实时监听：`getAllFavorites` 返回 `Flow`，收藏列表变更时 UI 自动刷新；

- 精准判断：`getFavorite` 通过双编码组合查询，避免重复收藏；

- 灵活删除：支持 “按编码”（收藏 / 取消收藏）和 “按主键”（收藏列表删除）两种删除方式，适配不同 UI 场景。

## 3. LIKE 关键字的使用方法和作用

### 3.1 使用方法

在 Room 的 SQL 查询中，`LIKE` 用于模糊匹配字符串，核心语法为：

```sql
SELECT * FROM airport WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery
```

- 匹配规则：需结合通配符使用（如 `%` 匹配任意字符），实际使用时需在 ViewModel 层拼接通配符（如 `%${query}%`）；

- 大小写兼容：Room 的 `LIKE` 关键字在 SQLite 中默认区分大小写，通过 ViewModel 的 `contains(ignoreCase = true)` 补足大小写不敏感匹配。

### 3.2 核心作用

- 实现机场名称 / 编码的模糊搜索，支持 “输入部分字符即可匹配” 的自动补全功能；

- 提升搜索灵活性：用户可输入 IATA 编码（如 “PEK”）或机场名称（如 “北京”），均能匹配到对应结果；

- 排序配合：结合 `ORDER BY passengers DESC`，热门机场优先展示，提升搜索效率。

## 4. 联合查询的实现和作用

### 4.1 实现方式

本应用通过 **Flow 组合** 实现 “联合查询”（Room 层无直接多表 JOIN，通过内存层组合数据），核心代码示例：

```kotlin
// 目的地列表：组合“选中机场状态”和“全量机场数据”
val destinations: StateFlow<List<Airport>> = _uiState
    .combine(flightDao.getAllAirports()) { state, allAirports ->
        state.selectedAirport?.let {
            allAirports.filter { it.iata_code != state.selectedAirport!!.iata_code }
        } ?: emptyList()
    }.stateIn(...)

// 收藏列表：组合“收藏数据”和“全量机场数据”（匹配机场名称）
FavoritesList(
    favorites = uiState.favorites,
    allAirports = allAirports,
    onDeleteFavorite = { viewModel.deleteFavorite(it) }
)
```

### 4.2 核心作用

- 多数据源联动：将 “用户状态（选中机场 / 搜索关键词）” 与 “数据库原始数据” 组合，动态生成符合业务逻辑的结果；

- 避免冗余 JOIN：预置数据库无外键关联，通过内存层组合 `favorite` 和 `airport` 数据，既匹配收藏航线的机场名称，又避免数据库表结构改造；

- 实时性保障：基于 `Flow.combine` 实现，任一数据源变更（如选中机场、收藏列表），结果实时更新。

## 5. Preferences DataStore 的使用场景和实现

### 5.1 使用场景

替代 SharedPreferences，用于存储轻量级、结构化的用户偏好数据，本应用中核心场景：

- 缓存最后一次搜索关键词，应用重启后恢复搜索状态；

- 缓存选中的出发机场编码，应用重启后恢复航班列表页面；

- 清空偏好设置（返回首页），重置搜索和选中状态。

### 5.2 实现方式

#### 5.2.1 定义 DataStore 实例和 Key

```kotlin
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

private object PreferencesKeys {
    val LAST_SEARCH_QUERY = stringPreferencesKey("last_search_query")  // 最后搜索关键词
    val SELECTED_AIRPORT = stringPreferencesKey("selected_airport")    // 选中机场编码
}
```

#### 5.2.2 封装读写方法

```kotlin
// 读取：返回Flow，实时监听数据变更
val lastSearchQuery: Flow<String> = dataStore.data
    .map { preferences -> preferences[PreferencesKeys.LAST_SEARCH_QUERY] ?: "" }

// 写入：挂起函数，在IO线程执行
suspend fun saveLastSearchQuery(query: String) {
    dataStore.edit { preferences ->
        preferences[PreferencesKeys.LAST_SEARCH_QUERY] = query
    }
}

// 清空：重置所有偏好
suspend fun clearPreferences() {
    dataStore.edit { it.clear() }
}
```

#### 5.2.3 ViewModel 层集成

```kotlin
// 初始化时读取缓存，恢复状态
init {
    viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.lastSearchQuery.collect { lastQuery ->
            _uiState.update { it.copy(searchQuery = lastQuery) }
        }
    }
}

// 操作时写入缓存
fun updateSearchQuery(query: String) {
    viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.saveLastSearchQuery(query)
        _uiState.update { ... }
    }
}
```

## 6. ViewModel 状态管理设计

### 6.1 核心状态封装

通过 `FlightUiState` 数据类封装所有 UI 状态，保证状态的不可变性：

```kotlin
data class FlightUiState(
    val searchQuery: String = "",        // 搜索关键词
    val selectedAirport: Airport? = null,// 选中的出发机场
    val favorites: List<Favorite> = emptyList()  // 收藏列表
)
```

### 6.2 响应式状态流设计

- 私有可变流：`_uiState = MutableStateFlow(FlightUiState())`，内部更新状态；

- 公开只读流：`uiState = _uiState.asStateFlow()`，UI 层仅能观察，不可修改；

- 派生状态流：通过 `combine` 组合基础流，生成搜索结果、目的地列表等派生数据：

    ```kotlin
    val searchResults: StateFlow<List<Airport>> = _uiState
        .combine(flightDao.getAllAirports()) { state, allAirports ->
            if (state.searchQuery.isBlank()) emptyList()
            else allAirports.filter { ... }
        }.stateIn(...)
    ```

### 6.3 状态更新规则

- 线程安全：所有状态更新操作通过 `viewModelScope.launch(Dispatchers.IO)` 切换到 IO 线程；

- 数据一致性：状态更新与 DataStore 缓存写入原子性执行（如更新搜索关键词时，同时写入缓存 + 更新 UI 状态）；

- 实时监听：初始化时监听数据库和 DataStore 的数据流，自动同步状态：

    ```kotlin
    // 实时监听收藏列表
    viewModelScope.launch {
        flightDao.getAllFavorites().collect { favList ->
            _uiState.update { it.copy(favorites = favList) }
        }
    }
    ```

### 6.4 状态操作封装

将所有业务逻辑封装为 ViewModel 方法，UI 层仅调用方法，不直接操作状态：

```kotlin
fun updateSearchQuery(query: String)  // 更新搜索关键词
fun selectAirport(airport: Airport)   // 选中出发机场
fun clearSelection()                  // 清空选中状态
fun toggleFavorite(departureCode: String, destinationCode: String)  // 收藏/取消收藏
```

## 7. UI 切换逻辑说明

基于 `uiState` 的状态值，UI 分为三个核心场景，通过条件判断自动切换：

### 7.1 场景 1：已选择出发机场（展示航班列表）

```kotlin
uiState.selectedAirport != null -> {
    Text(text = "航班列表")
    DestinationsList(
        departureAirport = uiState.selectedAirport!!,
        destinations = destinations,
        favorites = uiState.favorites,
        onToggleFavorite = { ... }
    )
}
```

- 触发条件：用户选中搜索结果中的某一机场；

- UI 内容：展示出发机场→所有其他机场的航线列表，支持收藏 / 取消收藏；

- 交互：右上角返回按钮，点击清空选中状态，回到首页。

### 7.2 场景 2：有搜索内容且有结果（展示搜索建议）

```kotlin
searchResults.isNotEmpty() -> {
    Text(text = "自动补全建议")
    SearchResultList(
        results = searchResults,
        onSelectAirport = { ... }
    )
}
```

- 触发条件：用户输入搜索关键词，且匹配到机场数据；

- UI 内容：展示模糊匹配的机场列表，点击后切换到航班列表场景；

- 交互：输入框实时输入，结果实时更新。

### 7.3 场景 3：无搜索、无选中机场（展示收藏航线）

```kotlin
else -> {
    Text(text = "收藏航线")
    FavoritesList(
        favorites = uiState.favorites,
        allAirports = allAirports,
        onDeleteFavorite = { ... }
    )
}
```

- 触发条件：应用启动、清空选中状态、搜索关键词为空；

- UI 内容：展示用户收藏的航线列表，支持删除收藏；

- 空状态处理：无收藏时展示 “暂无收藏航线” 提示。

## 8. 实验中遇到的问题与解决过程

### 8.1 问题 1：预置数据库表字段不匹配导致崩溃

- 现象：`Favorite` 实体中定义的 `departureName`/`destinationName` 字段，预置数据库的 `favorite` 表无对应列，启动即崩溃；

- 解决过程：

    1. 排查崩溃日志，定位到 Room 数据库表结构不匹配问题；

    2. 删除 `Favorite` 实体中的冗余名称字段，仅保留与数据库一致的 `departure_code`/`destination_code`/`id`；

    3. 调整收藏列表 UI 逻辑，通过 `allAirports` 流匹配 IATA 编码获取机场名称，而非直接存储在收藏表中。

### 8.2 问题 2：状态恢复异常（应用重启后搜索 / 选中状态丢失）

- 现象：应用重启后，之前的搜索关键词、选中机场状态未恢复；

- 解决过程：

    1. 引入 Preferences DataStore 替代 SharedPreferences，封装偏好数据读写；

    2. ViewModel 初始化时，监听 DataStore 的 `lastSearchQuery` 和 `selectedAirport` 流，自动更新 UI 状态；

    3. 确保所有状态变更操作（如更新搜索关键词、选中机场）都同步写入 DataStore。

### 8.3 问题 3：收藏 / 取消收藏功能无效

- 现象：点击收藏按钮后，UI 未实时刷新，收藏状态判断错误；

- 解决过程：

    1. 排查 DAO 层方法，确保 `getFavorite` 方法使用 `suspend` 修饰，且查询条件为 `departure_code + destination_code` 组合；

    2. 调整 ViewModel 的 `toggleFavorite` 方法，在 IO 线程执行数据库操作；

    3. 确保 `getAllFavorites` 返回 `Flow` 类型，ViewModel 实时监听并更新 `uiState.favorites`，UI 层基于该状态判断收藏状态。

### 8.4 问题 4：搜索结果大小写敏感

- 现象：输入小写字母（如 “pek”）无法匹配大写编码（如 “PEK”）；

- 解决过程：

    1. 放弃 SQL 层的 `LIKE` 大小写兼容（SQLite 需额外配置）；

    2. 在 ViewModel 的搜索结果过滤逻辑中，使用 `contains(ignoreCase = true)` 实现大小写不敏感匹配：

        ```kotlin
        allAirports.filter {
            it.iata_code.contains(state.searchQuery, ignoreCase = true) ||
            it.name.contains(state.searchQuery, ignoreCase = true)
        }
        ```




