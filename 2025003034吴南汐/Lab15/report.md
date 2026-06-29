# Lab15 Flight Search 应用实验报告
## 一、实验概述
本次实验基于 Android Compose、Room 数据库、Preferences DataStore 开发航班查询应用，实现机场搜索自动补全、航班展示、航线收藏、搜索文本持久化等功能。

## 二、Entity 设计说明
### 1. Airport 实体
映射数据库 `airport` 表，一一对应表中字段，使用 Room 注解声明实体与主键。
```kotlin
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey val id: Int,
    val iata_code: String,
    val name: String,
    val passengers: Int
)
```

### 2. Favorite 实体
映射 `favorite` 表，存储收藏航线的机场代码，设置复合主键防止重复收藏。
```kotlin
@Entity(tableName = "favorite", primaryKeys = ["departure_code", "destination_code"])
data class Favorite(
    val departure_code: String,
    val destination_code: String
)
```

## 三、DAO 查询方法设计
DAO 封装所有数据库操作，结合 SQL 语句实现各类查询，返回 Flow 实现响应式更新。
1. **自动补全查询**：通过 `LIKE` 模糊匹配机场代码与名称，按客流量降序排序。
```kotlin
@Query("SELECT * FROM airport WHERE iata_code LIKE :key OR name LIKE :key ORDER BY passengers DESC")
fun getSuggestions(key: String): Flow<List<Airport>>
```
2. **航班查询**：根据出发机场代码，过滤掉自身，查询所有目的地机场。
```kotlin
@Query("SELECT * FROM airport WHERE iata_code != :departCode")
fun getFlights(departCode: String): Flow<List<Airport>>
```
3. **收藏查询**：使用联表查询，拼接出带机场名称的完整收藏航线。
4. **收藏操作**：提供 `@Insert`、`@Delete` 实现新增、取消收藏。

## 四、SQL LIKE 关键字
`LIKE` 用于**字符串模糊查询**，搭配通配符 `%` 实现搜索联想。
使用时在代码中拼接 `%关键词%`：
```kotlin
val searchKey = "%$inputText%"
```
结合 SQL 语句匹配包含输入内容的机场，完成自动补全。

## 五、联合查询（JOIN）
`favorite` 表只存机场代码，缺少名称。使用 `INNER JOIN` 关联 `favorite` 和 `airport` 两张表：
```sql
SELECT * FROM favorite
INNER JOIN airport d ON favorite.departure_code = d.iata_code
INNER JOIN airport a ON favorite.destination_code = a.iata_code
```
一次查询拿到完整航线信息，减少数据库访问次数，提升效率。

## 六、Preferences DataStore
### 使用场景
持久化保存用户搜索内容，应用重启后自动恢复文本；搜索框为空时展示收藏列表。

### 实现方式
定义存储键，通过 DataStore 读写数据，依托 Flow 异步监听数据变化，操作放在协程中执行。
```kotlin
private val SEARCH_KEY = stringPreferencesKey("search_text")
// 保存数据
suspend fun saveText(text: String) = dataStore.edit { it[SEARCH_KEY] = text }
// 读取数据
val textFlow: Flow<String> = dataStore.data.map { it[SEARCH_KEY] ?: "" }
```

## 七、ViewModel 状态管理
ViewModel 统一管理页面状态：搜索文本、联想列表、选中机场、航班与收藏列表。
借助 `StateFlow` 管理 UI 状态，配置变更（屏幕旋转）时数据不丢失，同时统一调度数据库、DataStore 相关业务逻辑。
```kotlin
private val _searchText = MutableStateFlow("")
val searchText: StateFlow<String> = _searchText
```

## 八、UI 切换逻辑
基于 Compose 搭建界面，依靠**搜索文本**和**选中机场**两个状态自动切换页面：
1. 搜索框为空：展示收藏航线列表；
2. 输入内容：展示机场自动补全列表；
3. 选中机场：展示对应航班列表，每条条目附带收藏按钮。
使用 `LazyColumn` 展示列表，`AnimatedVisibility` 控制界面显隐。

## 九、实验问题与解决
1. **自动补全无结果**
原因：未拼接 `%` 通配符。
解决：将用户输入拼接为 `%内容%` 后再传入查询。

2. **查询出自身航班**
原因：未过滤当前出发机场。
解决：SQL 中添加条件 `iata_code != 出发代码`。

3. **重启应用搜索文本丢失**
原因：DataStore 读取时机错误。
解决：ViewModel 初始化时读取本地存储，文本变更时实时保存。

4. **重复收藏航线**
原因：无重复校验。
解决：为收藏表设置复合主键，拦截重复收藏操作。