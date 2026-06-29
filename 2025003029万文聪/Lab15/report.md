# Lab15：Flight Search 应用实验报告

## 1. Entity 设计说明

### Airport（机场实体）

```kotlin
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "iata_code") val iataCode: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "passengers") val passengers: Int
)
```

- **表名**: `airport`，对应数据库中的机场表
- **主键**: `id`，自增整数，唯一标识每条机场记录
- **iata_code**: IATA 机场代码（3 个大写字母，如 "PEK" 表示北京首都国际机场）
- **name**: 机场全称
- **passengers**: 年客运量，用于排序和展示机场繁忙程度

**映射关系**: 该 Entity 直接映射 `flight_search.db` 中的 `airport` 表，每一列与数据库字段一一对应。采用 `@ColumnInfo` 注解明确指定列名，确保与数据库中的 snake_case 命名一致。

### Favorite（收藏实体）

```kotlin
@Entity(
    tableName = "favorite",
    indices = [Index(value = ["departure_code", "destination_code"], unique = true)]
)
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "departure_code") val departureCode: String,
    @ColumnInfo(name = "destination_code") val destinationCode: String
)
```

- **表名**: `favorite`，对应数据库中的收藏表
- **主键**: `id`，自增整数
- **departure_code**: 出发地机场的 IATA 代码
- **destination_code**: 目的地机场的 IATA 代码
- **索引**: 在 `(departure_code, destination_code)` 上创建唯一索引，防止重复收藏同一条航线

**设计说明**: Favorite 表只存储机场代码（关联到 airport 表），不直接存储名称，这样设计减少了数据冗余。当需要显示机场名称时，通过 SQL 联合查询从 airport 表获取。

---

## 2. DAO 查询方法设计说明

### 2.1 自动补全查询

```kotlin
@Query("""
    SELECT * FROM airport
    WHERE iata_code LIKE :query OR name LIKE :query
    ORDER BY passengers DESC
""")
fun searchAirports(query: String): Flow<List<Airport>>
```

- 接收带有 `%` 通配符的搜索关键词
- 同时在 `iata_code` 列和 `name` 列进行模糊匹配
- 按 `passengers` 降序排列，确保热门的机场优先显示
- 返回 `Flow<List<Airport>>` 实现响应式查询

### 2.2 航班查询

```kotlin
@Query("""
    SELECT * FROM airport
    WHERE iata_code != :departureCode
    ORDER BY passengers DESC
""")
fun getDestinations(departureCode: String): Flow<List<Airport>>
```

- 根据用户选择的机场代码，查询所有可能的目的地
- 使用 `!=` 排除出发地自身（不能飞回同一个机场）
- 按乘客量降序排列，优先显示热门目的地

### 2.3 收藏查询（联合查询）

```kotlin
@Query("""
    SELECT
        favorite.id AS favoriteId,
        favorite.departure_code AS departureCode,
        favorite.destination_code AS destinationCode,
        departureAirport.name AS departureName,
        departureAirport.passengers AS departurePassengers,
        destinationAirport.name AS destinationName,
        destinationAirport.passengers AS destinationPassengers
    FROM favorite
    INNER JOIN airport AS departureAirport
        ON favorite.departure_code = departureAirport.iata_code
    INNER JOIN airport AS destinationAirport
        ON favorite.destination_code = destinationAirport.iata_code
    ORDER BY destinationAirport.passengers DESC
""")
fun getAllFavorites(): Flow<List<FavoriteWithAirports>>
```

- 通过两次 `INNER JOIN` 关联 airport 表，同时获取出发地和目的地的详细信息
- 返回自定义的 `FavoriteWithAirports` 数据类，包含航线两端的完整信息

### 2.4 增删收藏

```kotlin
@Insert(onConflict = OnConflictStrategy.IGNORE)
suspend fun addFavorite(favorite: Favorite)

@Query("DELETE FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
suspend fun deleteFavorite(departureCode: String, destinationCode: String)

@Query("SELECT COUNT(*) FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
suspend fun isFavorite(departureCode: String, destinationCode: String): Int
```

- `addFavorite` 使用 `OnConflictStrategy.IGNORE` 避免重复插入
- `deleteFavorite` 通过出发地和目的地代码精确定位删除
- `isFavorite` 通过计数查询判断是否已收藏

---

## 3. LIKE 关键字的使用方法和作用

### 基本语法

```sql
SELECT * FROM airport
WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery
ORDER BY passengers DESC
```

### 使用方法

在 Room DAO 中，LIKE 关键字结合通配符 `%` 实现模糊搜索：

```kotlin
// 在 ViewModel 中调用时添加通配符
dao.searchAirports("%$query%")
```

### 作用

1. **模糊匹配**：LIKE 允许在字符串模式中进行部分匹配，而不仅仅是精确匹配
2. **通配符 `%`**：匹配零个或多个任意字符，例如 `"%PE%"` 可以匹配 "PEK"、"PER" 等
3. **双列搜索**：同时在 IATA 代码和机场名称两列上应用 LIKE，用户无论输入代码还是名称都能得到匹配结果
4. **渐进式搜索**：随着用户输入更多字符，搜索结果逐步精确，实现自动补全体验

### 注意事项

- 需要在参数前后手动添加 `%` 通配符
- `LIKE` 在 SQLite 中默认不区分大小写，适合用户输入不规范的场景

---

## 4. 联合查询的实现和作用

### 实现方式

```sql
SELECT
    favorite.id AS favoriteId,
    favorite.departure_code AS departureCode,
    favorite.destination_code AS destinationCode,
    departureAirport.name AS departureName,
    destinationAirport.name AS destinationName
FROM favorite
INNER JOIN airport AS departureAirport
    ON favorite.departure_code = departureAirport.iata_code
INNER JOIN airport AS destinationAirport
    ON favorite.destination_code = destinationAirport.iata_code
```

### 作用

1. **数据归一化**：Favorite 表只存机场代码，通过 JOIN 从 airport 表获取完整名称，避免数据冗余
2. **两次 JOIN**：同一张 airport 表被 JOIN 两次（别名 `departureAirport` 和 `destinationAirport`），分别获取出发地和目的地的信息
3. **性能优化**：相比在应用层多次查询，一次 JOIN 查询即可获取所有需要的数据，减少数据库访问次数
4. **数据一致性**：机场名称始终从 airport 表获取，保证显示与存储一致

### 在 Room 中的实现

定义了一个专用的数据类来接收 JOIN 查询结果：

```kotlin
data class FavoriteWithAirports(
    val favoriteId: Int,
    val departureCode: String,
    val destinationCode: String,
    val departureName: String,
    val departurePassengers: Int,
    val destinationName: String,
    val destinationPassengers: Int
)
```

Room 自动将查询结果的列映射到该数据类的属性。

---

## 5. Preferences DataStore 的使用场景和实现

### 使用场景

Preferences DataStore 用于持久化用户的搜索文本状态，使得应用重启后能恢复上次的搜索状态：

1. **搜索文本持久化**：用户输入搜索内容后退出应用，再次打开时搜索框自动填充上次的内容
2. **自动恢复界面状态**：如果退出时搜索文本不为空，重新打开后自动进入自动补全/航班列表界面
3. **轻量级存储**：仅保存一个字符串键值对，适合使用 Preferences DataStore 而非 Proto DataStore

### 实现方式

**创建 DataStore 实例**：

```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "flight_search_preferences"
)
```

**定义键和操作**：

```kotlin
val SEARCH_TEXT = stringPreferencesKey("search_text")

val searchTextFlow: Flow<String> = context.dataStore.data
    .map { preferences -> preferences[SEARCH_TEXT] ?: "" }

suspend fun saveSearchText(text: String) {
    context.dataStore.edit { preferences ->
        preferences[SEARCH_TEXT] = text
    }
}
```

### 在 ViewModel 中的集成

```kotlin
// 初始化时读取保存的搜索文本
init {
    viewModelScope.launch {
        val savedText = preferencesRepository.searchTextFlow.first()
        _searchText.value = savedText
        if (savedText.isNotBlank()) {
            updateDebouncedSearch(savedText)
            _uiState.value = FlightUiState.Autocomplete
        }
    }
}

// 用户选择机场时保存搜索文本
fun onAirportSelected(airport: Airport) {
    _searchText.value = "${airport.name} (${airport.iataCode})"
    // ...
    saveCurrentSearchText()
}
```

### DataStore vs SharedPreferences

| 特性 | DataStore | SharedPreferences |
|------|-----------|-------------------|
| 异步 API | ✅ Flow 支持 | ❌ 同步 API |
| 类型安全 | ✅ | ❌ |
| 主线程安全 | ✅ 自动在 Dispatchers.IO 执行 | ❌ |
| 错误处理 | ✅ 抛出异常 | ❌ 静默失败 |
| 事务支持 | ✅ | ❌ |

---

## 6. ViewModel 状态管理设计

### UI 状态模型

使用密封类表示三种不同的 UI 界面：

```kotlin
sealed class FlightUiState {
    data object FavoritesList : FlightUiState()    // 收藏列表
    data object Autocomplete : FlightUiState()     // 自动补全
    data class FlightList(val departureAirport: Airport) : FlightUiState() // 航班列表
}
```

### 状态管理方案

ViewModel 管理以下核心状态：

| 状态 | 类型 | 说明 |
|------|------|------|
| `_searchText` | `MutableStateFlow<String>` | 搜索框的当前文本 |
| `_debouncedSearchText` | `MutableStateFlow<String>` | 防抖后的搜索文本（300ms 延迟） |
| `_uiState` | `MutableStateFlow<FlightUiState>` | 当前显示的界面类型 |
| `_selectedAirport` | `MutableStateFlow<Airport?>` | 用户选择的出发机场 |
| `_destinations` | `MutableStateFlow<List<Airport>>` | 航班目的地列表 |
| `favorites` | `StateFlow<List<FavoriteWithAirports>>` | 收藏列表（来自 Room Flow） |
| `_favoriteSet` | `MutableStateFlow<Set<String>>` | 收藏集合（快速查询） |

### 状态流转

```
初始状态
    ↓
搜索框为空 → FavoritesList（显示收藏）
    ↓
用户输入 → Autocomplete（显示建议）
    ↓
选择机场 → FlightList（显示航班）
    ↓
清除搜索 → FavoritesList（回到收藏）
```

### 防抖机制

```kotlin
fun onSearchTextChanged(text: String) {
    _searchText.value = text
    searchJob?.cancel()
    if (text.isBlank()) {
        _uiState.value = FlightUiState.FavoritesList
        return
    }
    searchJob = viewModelScope.launch {
        delay(300)  // 300ms 防抖
        updateDebouncedSearch(text)
        _uiState.value = FlightUiState.Autocomplete
    }
}
```

防抖避免每次按键都触发数据库查询，仅在用户停止输入 300ms 后才发起查询。

---

## 7. UI 切换逻辑说明

### 整体布局

UI 采用 `Box` 容器包裹内容区域，根据 `FlightUiState` 切换显示不同内容：

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    when (val state = uiState) {
        is FlightUiState.FavoritesList -> FavoritesListContent(...)
        is FlightUiState.Autocomplete -> AutocompleteContent(...)
        is FlightUiState.FlightList -> FlightListContent(...)
    }
}
```

### 各界面说明

1. **搜索栏**：始终显示在顶部，提供输入和清除功能
2. **收藏列表**：搜索框为空时展示，显示所有已保存的航线，含空状态提示
3. **自动补全**：用户输入时展示匹配机场列表，每项显示 IATA 代码和名称
4. **航班列表**：选择机场后展示所有目的地，每项含收藏按钮

### 交互流程

```
1. 用户打开应用
   ├── 上次有搜索 → 恢复搜索文本，显示自动补全/航班列表
   └── 上次无搜索 → 显示收藏列表

2. 用户输入搜索
   ├── 输入文本 → 300ms 防抖 → 自动补全建议
   ├── 点击建议 → 切换到航班列表
   └── 清除文本 → 回到收藏列表

3. 收藏操作
   ├── 点击收藏按钮（空心） → 添加收藏
   └── 点击已收藏按钮（实心） → 取消收藏
```

### 动画效果

使用 Material3 的 `Card` 组件提供自然的层次感和交互动画，收藏按钮的图标切换提供即时视觉反馈。

---

## 8. 实验中遇到的问题与解决过程

### 问题 1：数据库文件加载失败

**现象**: Room 使用 `createFromAsset()` 时无法找到数据库文件。

**解决**: 将 `flight_search.db` 放置在 `assets/database/` 目录下，确保路径与 `createFromAsset("database/flight_search.db")` 一致。同时添加了 `fallbackToDestructiveMigration()` 防止版本不匹配导致的崩溃。

### 问题 2：自动补全查询频率过高

**现象**: 用户每次按键都会触发数据库查询，导致 UI 卡顿和性能浪费。

**解决**: 引入防抖机制（Debounce），在 `onSearchTextChanged` 中使用 `delay(300)` 延迟查询，仅当用户停止输入 300ms 后才执行数据库搜索。

### 问题 3：收藏状态实时更新

**现象**: 添加/删除收藏后，UI 界面没有及时反映状态变化。

**解决**: 使用 Room 的 `Flow` 返回值实现响应式查询，ViewModel 监听 `favorites` Flow 的变化，自动更新 `_favoriteSet` 集合，UI 层通过 `collectAsState()` 自动重组。

### 问题 4：联合查询结果映射

**现象**: `INNER JOIN` 查询返回的结果无法直接映射到已有 Entity。

**解决**: 定义 `FavoriteWithAirports` 数据类，使用 `AS` 别名将查询列映射到数据类属性，Room 自动完成映射。

### 问题 5：搜索文本状态恢复

**现象**: 应用旋转屏幕后，ViewModel 状态丢失。

**解决**: 使用 `AndroidViewModel` 而非 `ViewModel`，确保在配置变更后 ViewModel 实例仍然存活。同时使用 DataStore 持久化搜索文本，即使进程被杀死也能恢复。

### 问题 6：DataStore 实例化

**现象**: 多个组件需要访问 DataStore，导致创建多个实例。

**解决**: 使用单例模式和 `applicationContext` 确保 DataStore 和 UserPreferencesRepository 在整个应用生命周期中只有一个实例。

---

## 附件说明

- `screenshot_search.png`: 搜索自动补全界面截图
- `screenshot_flights.png`: 航班列表界面截图
- `screenshot_favorites.png`: 收藏列表界面截图

（截图文件需在模拟器或真机上运行应用后截取）
