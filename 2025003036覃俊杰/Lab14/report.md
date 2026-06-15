# Lab14 实验报告 — 使用 Room 完成 Bus Schedule 应用

## 1. Entity、DAO、Database 在本实验中的职责

| 组件 | 职责 |
|------|------|
| **Entity** (`BusSchedule`) | 映射数据库中的 `Schedule` 表，将每一行数据映射为 Kotlin 对象。每个属性对应一个数据库列。 |
| **DAO** (`BusScheduleDao`) | 定义数据访问方法，通过 `@Query` 注解编写 SQL 语句，返回 `Flow<List<BusSchedule>>` 供上层使用。 |
| **Database** (`BusScheduleDatabase`) | 持有数据库连接，提供 DAO 实例，使用单例模式确保全局只有一个实例。通过 `createFromAsset()` 加载预置数据库。 |

三者协作关系：`Database` -> `DAO` -> `Entity`，数据从 SQLite 表经 Room 映射后流过 DAO 到达 ViewModel。

## 2. BusSchedule 属性与 Schedule 表的映射

| Kotlin 属性 | 数据库列 | 注解 |
|------------|----------|------|
| `id` | `id` | `@PrimaryKey` |
| `stopName` | `stop_name` | `@ColumnInfo(name = "stop_name")` |
| `arrivalTimeInMillis` | `arrival_time` | `@ColumnInfo(name = "arrival_time")` |

Entity 定义：
```kotlin
@Entity(tableName = "Schedule")
data class BusSchedule(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "stop_name") val stopName: String,
    @ColumnInfo(name = "arrival_time") val arrivalTimeInMillis: Int
)
```
表名必须写 `Schedule`（与预置数据库一致），`arrivalTimeInMillis` 沿用起始代码命名以避免 UI 层改动。

## 3. DAO 查询语句的作用与排序原因

```kotlin
@Query("SELECT * FROM Schedule ORDER BY arrival_time ASC")
fun getAll(): Flow<List<BusSchedule>>

@Query("SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC")
fun getByStopName(stopName: String): Flow<List<BusSchedule>>
```

- `getAll()`：获取完整时刻表，用于首页列表展示。
- `getByStopName(stopName)`：按站点名称过滤，用于详情页展示该站点的所有到站时间。

**按 `arrival_time ASC` 排序**是因为公交时刻表应按时间先后顺序展示，方便乘客查看各站点的到站时间，符合实际使用习惯。

## 4. `createFromAsset("database/bus_schedule.db")` 的作用

该方法告诉 Room 在首次创建数据库时，从 `app/src/main/assets/database/bus_schedule.db` 复制预置数据到应用的内部数据库目录。这样应用第一次运行时就能直接使用完整的公交时刻数据，而无需手动插入或网络下载。

## 5. ViewModel 从示例数据切换为数据库数据

**修改前**：ViewModel 使用 `flowOf()` 返回写死的示例数据：
```kotlin
fun getFullSchedule(): Flow<List<BusSchedule>> = flowOf(
    listOf(BusSchedule(0, "Example Street", 0))
)
```

**修改后**：ViewModel 构造函数接收 `BusScheduleDao`，方法直接调用 DAO 查询：
```kotlin
class BusScheduleViewModel(
    private val busScheduleDao: BusScheduleDao
) : ViewModel() {
    fun getFullSchedule(): Flow<List<BusSchedule>> = busScheduleDao.getAll()
    fun getScheduleFor(stopName: String): Flow<List<BusSchedule>> = busScheduleDao.getByStopName(stopName)
}
```
通过 `viewModelFactory` 配合 `APPLICATION_KEY` 获取 Application Context 来创建数据库和 DAO 实例，ViewModel 不再持有任何硬编码数据。

## 6. Flow\<List\<BusSchedule\>\> 被 Compose 页面收集与显示

在 `BusScheduleScreens.kt` 中，Composable 函数通过 `viewModel` 调用方法获得 `Flow`，再使用 `collectAsState()` 将其转换为 Compose 可观察的状态：

```kotlin
val schedule by viewModel.getFullSchedule().collectAsState(initial = emptyList())
```

当 Room 数据库中的数据变化时，Flow 自动发射新数据，`collectAsState()` 感知到变化后触发重组（Recomposition），UI 随之更新。整个过程响应式且无需手动刷新。

## 7. 实验中遇到的问题与解决过程

| 问题 | 解决过程 |
|------|----------|
| 运行后仍显示 "Example Street" | 检查 ViewModel，发现 `getFullSchedule()` 还在用 `flowOf()` 返回示例数据，删除旧代码并改为调用 DAO 后解决。 |
| Room 编译报错 "Not sure how to convert a Cursor to this method's return type" | 检查 Entity 属性名与数据库列名不一致，补全 `@ColumnInfo` 注解后解决。 |
| 查询结果为空，首页不显示数据 | DAO 的 SQL 中 `WHERE stop_name = :stopName` 参数与传入的站点名称大小写不匹配，统一字符串格式后解决。 |
| 数据库表找不到异常 | 确认 `@Entity(tableName = "Schedule")` 表名与数据库中一致，注意不能写成 `BusSchedule`。 |
