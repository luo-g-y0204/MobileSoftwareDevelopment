# Lab15：Flight Search 应用项目

## 一、Entity 设计说明

### 1. Airport 实体（对应 airport 表）

```kotlin
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val iata_code: String,
    val name: String,
    val passengers: Int
)
```

- **映射规则**：严格匹配预置数据库 `airport` 表结构，每个字段与数据库列一一对应。

- **字段说明**：

    - `id`：自增主键，对应数据库 `id` 列（INTEGER 类型）；

    - `iata_code`：机场三字码，对应 `iata_code` 列（VARCHAR 类型）；

    - `name`：机场全称，对应 `name` 列（VARCHAR 类型）；

    - `passengers`：年客流量，对应 `passengers` 列（INTEGER 类型），用于搜索结果按客流量降序排序。

### 2. Favorite 实体（对应 favorite 表）

```kotlin
@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val departure_code: String,
    val destination_code: String
)
```

- **映射规则**：匹配预置数据库 `favorite` 表结构，仅保留数据库中存在的列，移除了自定义的 `departureName`/`destinationName` 字段（避免数据库读写崩溃）。

- **字段说明**：

    - `id`：自增主键，对应数据库 `id` 列；

    - `departure_code`：出发机场三字码，对应 `departure_code` 列；

    - `destination_code`：目的机场三字码，对应 `destination_code` 列。

## 二、DAO 查询方法设计说明

### 1. 自动补全查询（搜索机场）

```kotlin
@Query("SELECT * FROM airport WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery ORDER BY passengers DESC")
fun searchAirports(searchQuery: String): Flow<List<Airport>>
```

- **功能**：根据用户输入的关键词，匹配机场的 `iata_code` 或 `name`，并按客流量降序返回结果，实现自动补全。

- **设计要点**：

    - 使用 `LIKE` 关键字实现模糊匹配；

    - 返回 `Flow<List<Airport>>`，支持数据实时监听；

    - 按 `passengers` 降序排序，优先显示客流量大的机场，符合用户使用习惯。

### 2. 航班查询（获取目的地列表）

```kotlin
@Query("SELECT * FROM airport WHERE iata_code != :departureCode ORDER BY name ASC")
fun getDestinations(departureCode: String): Flow<List<Airport>>
```

- **功能**：排除当前选中的出发机场，返回所有可作为目的地的机场列表。

- **设计要点**：

    - 通过 `iata_code != :departureCode` 排除出发机场自身；

    - 按机场名称升序排序，保证列表展示有序；

    - 返回 `Flow` 类型，支持目的地列表实时更新。

### 3. 收藏查询

#### （1）获取所有收藏

```kotlin
@Query("SELECT * FROM favorite")
fun getAllFavorites(): Flow<List<Favorite>>
```

- **功能**：实时监听收藏表数据，返回所有收藏的航线。

- **设计要点**：返回 `Flow` 类型，收藏数据变更时自动通知 UI 更新。

#### （2）查询单条收藏（判断是否已收藏）

```kotlin
@Query("SELECT * FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode LIMIT 1")
suspend fun getFavorite(departureCode: String, destinationCode: String): Favorite?
```

- **功能**：校验指定出发 / 目的机场的航线是否已收藏，用于收藏按钮状态切换。

- **设计要点**：

    - 限定 `LIMIT 1`，提升查询效率；

    - 挂起函数（`suspend`），在协程中执行，避免主线程阻塞。

#### （3）收藏增删操作

```kotlin
@Insert
suspend fun insertFavorite(favorite: Favorite)

@Query("DELETE FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
suspend fun deleteFavorite(departureCode: String, destinationCode: String)

@Query("DELETE FROM favorite WHERE id = :id")
suspend fun deleteFavoriteById(id: Int)
```

- **功能**：实现收藏的添加、按航线删除、按 ID 删除，覆盖收藏功能全场景。

- **设计要点**：均为挂起函数，保证数据库操作在后台执行。

## 三、`LIKE` 关键字的使用方法和作用

### 1. 使用方法

`LIKE` 是 SQL 中用于模糊匹配字符串的关键字，结合通配符 `%` 使用：

- `%xxx`：匹配以 `xxx` 结尾的字符串；

- `xxx%`：匹配以 `xxx` 开头的字符串；

- `%xxx%`：匹配包含 `xxx` 的任意位置字符串。

在本实验中，实际使用时需在搜索关键词前后拼接 `%`，例如：

```kotlin
// 构造 LIKE 查询参数
val searchQuery = "%${inputQuery}%"
flightDao.searchAirports(searchQuery)
```

### 2. 作用

- **核心作用**：实现机场名称 / 三字码的模糊搜索，满足用户 “输入部分字符即可匹配相关机场” 的自动补全需求；

- **优势**：相比精确匹配，`LIKE` 关键字大幅提升搜索的灵活性和用户体验，符合 “自动补全” 的功能设计目标；

- **配合排序**：结合 `ORDER BY passengers DESC`，让高客流量机场优先显示，进一步优化搜索结果的实用性。

## 四、联合查询的实现和作用

### 1. 实现方式

本实验中，收藏列表需要展示 “机场代码 + 机场名称”，但 `favorite` 表仅存储代码，因此需联合 `airport` 表查询：

```sql
-- 逻辑层面的联合查询（UI 层拼接实现）
SELECT f.*, dep.name as dep_name, dest.name as dest_name 
FROM favorite f
INNER JOIN airport dep ON f.departure_code = dep.iata_code
INNER JOIN airport dest ON f.destination_code = dest.iata_code
```

代码中通过 “先查收藏列表，再匹配机场列表” 实现联合查询效果：

```kotlin
// FavoritesList 组件中
val depAir = allAirports.firstOrNull { it.iata_code == fav.departure_code }
val destAir = allAirports.firstOrNull { it.iata_code == fav.destination_code }
```

### 2. 作用

- **数据补全**：解决 `favorite` 表仅存储代码、无法直接展示机场名称的问题，让收藏列表展示更完整的信息；

- **性能优化**：通过本地内存中匹配（`firstOrNull`）替代数据库层联合查询，减少数据库 IO 开销；

- **解耦设计**：数据库层仅负责基础数据查询，UI 层负责数据拼接，符合 “单一职责” 原则。

## 五、Preferences DataStore 的使用场景和实现

### 1. 使用场景

- **保存最后一次搜索关键词**：应用重启后自动填充搜索框，恢复用户上次操作状态；

- **保存选中的机场代码**：应用重启 / 屏幕旋转后，恢复用户选中的出发机场，避免状态丢失；

- **清空偏好设置**：用户返回首页时，清空搜索关键词和选中机场，恢复初始状态。

### 2. 实现方式

#### （1）定义 DataStore 实例和键

```kotlin
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

private object PreferencesKeys {
    val LAST_SEARCH_QUERY = stringPreferencesKey("last_search_query")
    val SELECTED_AIRPORT = stringPreferencesKey("selected_airport")
}
```

#### （2）数据读写 API

```kotlin
// 读取（返回 Flow，支持实时监听）
val lastSearchQuery: Flow<String> = dataStore.data.map { it[LAST_SEARCH_QUERY] ?: "" }
val selectedAirport: Flow<String> = dataStore.data.map { it[SELECTED_AIRPORT] ?: "" }

// 写入（挂起函数，协程中执行）
suspend fun saveLastSearchQuery(query: String) {
    dataStore.edit { it[LAST_SEARCH_QUERY] = query }
}
suspend fun saveSelectedAirport(airportCode: String) {
    dataStore.edit { it[SELECTED_AIRPORT] = airportCode }
}

// 清空
suspend fun clearPreferences() {
    dataStore.edit { it.clear() }
}
```

#### （3）ViewModel 中集成

```kotlin
// 初始化时读取 DataStore 数据
init {
    viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.lastSearchQuery.collect { lastQuery ->
            _uiState.update { it.copy(searchQuery = lastQuery) }
        }
    }
    viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.selectedAirport.collect { selectedCode ->
            if (selectedCode.isNotEmpty()) {
                val airport = flightDao.getAirportByCode(selectedCode)
                _uiState.update { it.copy(selectedAirport = airport) }
            }
        }
    }
}
```

## 六、ViewModel 状态管理设计

### 1. 核心状态容器

```kotlin
data class FlightUiState(
    val searchQuery: String = "",       // 搜索关键词
    val selectedAirport: Airport? = null, // 选中的出发机场
    val favorites: List<Favorite> = emptyList() // 收藏列表
)
```

### 2. 状态流转设计

- **MutableStateFlow 私有持有**：`_uiState` 为私有可变状态，对外暴露不可变的 `uiState`（`asStateFlow`）；

- **组合式数据流**：

    - `searchResults`：结合 `searchQuery` 和 `allAirports`，实时生成搜索结果；

    - `destinations`：结合 `selectedAirport` 和 `allAirports`，实时生成目的地列表；

- **协程作用域**：所有异步操作（数据库、DataStore）均在 `viewModelScope` 中执行，生命周期与 ViewModel 绑定；

- **状态更新逻辑**：

    - 搜索关键词更新：同步保存到 DataStore + 清空选中机场；

    - 选中机场更新：同步保存到 DataStore + 清空搜索关键词；

    - 收藏列表更新：实时监听数据库，自动同步到 UI 状态。

### 3. 对外暴露的操作 API

```kotlin
fun updateSearchQuery(query: String) // 更新搜索关键词
fun selectAirport(airport: Airport)  // 选中机场
fun clearSelection()                 // 清空选中状态
fun toggleFavorite(departureCode: String, destinationCode: String) // 收藏/取消收藏
fun deleteFavorite(favorite: Favorite) // 删除收藏
```

## 七、UI 切换逻辑说明

UI 状态根据 “搜索关键词” 和 “选中机场” 两个核心状态自动切换，分为三个分支：

### 1. 分支 1：已选择出发机场（显示航班列表）

```kotlin
uiState.selectedAirport != null -> {
    DestinationsList(/* 航班列表组件 */)
}
```

- **触发条件**：用户从搜索建议中选中某一机场；

- **展示内容**：该机场作为出发地的所有目的地航班，包含收藏按钮；

- **交互**：可对航班进行收藏 / 取消收藏操作，点击返回按钮清空选中状态。

### 2. 分支 2：有搜索内容且有结果（显示搜索建议）

```kotlin
searchResults.isNotEmpty() -> {
    SearchResultList(/* 搜索建议组件 */)
}
```

- **触发条件**：用户在搜索框输入关键词，且匹配到机场；

- **展示内容**：按客流量降序的机场列表，点击可选中该机场；

- **交互**：点击某一机场，切换到航班列表界面。

### 3. 分支 3：无搜索、无选中机场（显示收藏列表）

```kotlin
else -> {
    FavoritesList(/* 收藏列表组件 */)
}
```

- **触发条件**：搜索框为空且未选中任何机场；

- **展示内容**：用户收藏的所有航线，无收藏时显示 “暂无收藏航线”；

- **交互**：可删除收藏的航线。

### 4. 状态切换流程图

```Plain Text
初始状态（收藏列表）
    ↓（输入搜索关键词）
搜索建议列表
    ↓（选中机场）
航班列表
    ↓（点击返回/清空搜索）
收藏列表
```

## 八、实验中遇到的问题与解决过程

### 问题 1：数据库读写崩溃（Favorite 实体字段不匹配）

- **现象**：插入 / 查询 Favorite 时，应用崩溃，提示 “表 favorite 无 departureName 列”；

- **原因**：Favorite 实体中自定义了 `departureName`/`destinationName` 字段，但预置数据库 `favorite` 表无该列；

- **解决**：删除 Favorite 实体中多余的字段，仅保留数据库中存在的 `id`/`departure_code`/`destination_code`。

### 问题 2：搜索结果无数据（LIKE 查询未加通配符）

- **现象**：输入关键词后，搜索建议列表为空；

- **原因**：直接使用用户输入的关键词作为 LIKE 参数，未拼接 `%` 通配符，导致仅匹配完全一致的字符串；

- **解决**：在 ViewModel 的 `searchResults` 数据流中，自动为搜索关键词拼接 `%`（实际通过 Room 查询的特性，在 DAO 层隐式处理，或在调用时拼接）。

### 问题 3：应用重启后状态丢失（未监听 DataStore 数据）

- **现象**：应用重启后，搜索框为空，选中的机场也丢失；

- **原因**：ViewModel 初始化时未读取 DataStore 中保存的偏好设置；

- **解决**：在 ViewModel 的 `init` 块中，监听 `lastSearchQuery` 和 `selectedAirport` 数据流，同步到 UI 状态。

### 问题 4：收藏按钮状态不实时更新

- **现象**：点击收藏按钮后，按钮文字未立即切换（仍显示 “收藏”/“取消收藏”）；

- **原因**：收藏操作仅修改数据库，未实时同步到 ViewModel 的 `favorites` 状态；

- **解决**：ViewModel 中实时监听 `flightDao.getAllFavorites()` 数据流，自动更新 `uiState.favorites`，UI 层通过 `collectAsState` 实时刷新。

### 问题 5：屏幕旋转后状态丢失

- **现象**：旋转屏幕后，当前选中的机场 / 搜索关键词丢失；

- **原因**：未使用 StateFlow 管理 UI 状态，且未通过 DataStore 持久化；

- **解决**：

    1. 使用 StateFlow 存储 UI 状态，Compose 侧通过 `collectAsState` 收集；

    2. 关键状态（搜索关键词、选中机场）持久化到 DataStore，屏幕旋转后重新读取。

## 总结

本次实验完成了 Flight Search 应用的核心功能：机场搜索、航班列表展示、收藏管理、状态持久化。通过 Room 实现数据库操作，DataStore 实现状态持久化，ViewModel + StateFlow 实现状态管理，Compose 实现 UI 构建。实验过程中解决了数据库字段不匹配、LIKE 查询使用不当、状态持久化等问题，最终实现了符合要求的、状态稳定的航班搜索应用。

