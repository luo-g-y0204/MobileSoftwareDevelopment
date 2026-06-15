# Lab14 实验报告

## 一、实验概述

本次实验基于 Bus Schedule（公交时刻表）应用，练习使用 Room 读取本地预置数据库，将数据库中的公交站点与到站时间显示到 Jetpack Compose 界面中。

## 二、Entity、DAO、Database 三者职责说明

### 2.1 Entity（实体）
`Entity` 是 Room 数据库中的表结构映射。本实验中的 `BusSchedule` 就是 Entity，它定义了数据库表 `Schedule` 的结构：
- 每个 `BusSchedule` 实例对应表中的一条记录
- 属性通过注解映射到数据库列

### 2.2 DAO（数据访问对象）
`DAO` 是访问数据库的接口，提供抽象的数据库操作方法：
- `getAll()`: 获取所有公交时刻数据
- `getByStopName(stopName)`: 获取指定站点的所有到站时间
- 使用 `@Query` 注解编写 SQL 语句
- 返回 `Flow<List<BusSchedule>>` 实现响应式数据流

### 2.3 Database（数据库）
`Database` 是 Room 数据库的持有类：
- 继承自 `RoomDatabase`
- 包含 Entity 和 DAO 的声明
- 提供单例模式获取数据库实例
- 使用 `createFromAsset()` 加载预置数据库

## 三、BusSchedule 属性映射关系

`BusSchedule` 类的属性与 `Schedule` 表的映射关系如下：

| Kotlin 属性 | 数据库列 | 说明 |
|------------|----------|------|
| `id` | `id` | 主键，使用 `@PrimaryKey` 注解 |
| `stopName` | `stop_name` | 公交站点名称，使用 `@ColumnInfo(name = "stop_name")` |
| `arrivalTimeInMillis` | `arrival_time` | 到站时间戳，使用 `@ColumnInfo(name = "arrival_time")` |

```kotlin
@Entity(tableName = "Schedule")
data class BusSchedule(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "stop_name") val stopName: String,
    @ColumnInfo(name = "arrival_time") val arrivalTimeInMillis: Int
)
```

注意：表名是 `Schedule`，不是 `BusSchedule`，否则 Room 无法正确读取预置数据库。

## 四、DAO 查询语句说明

### 4.1 getAll() 查询
```kotlin
@Query("SELECT * FROM Schedule ORDER BY arrival_time ASC")
fun getAll(): Flow<List<BusSchedule>>
```
作用：获取完整公交时刻表，返回所有记录。

### 4.2 getByStopName() 查询
```kotlin
@Query("SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC")
fun getByStopName(stopName: String): Flow<List<BusSchedule>>
```
作用：根据站点名称筛选记录，返回该站点的所有到站时间。

### 4.3 排序说明
两条查询都使用 `ORDER BY arrival_time ASC`，按到站时间升序排序：
- 首页显示时，用户可以按时间顺序查看各站点
- 详情页显示时，同一站点的多个到站时间按先后顺序排列

## 五、createFromAsset() 的作用

```kotlin
Room.databaseBuilder(...)
    .createFromAsset("database/bus_schedule.db")
    .build()
```

`createFromAsset()` 用于从 `assets/database/` 目录加载预置的 SQLite 数据库文件：
- 数据库文件位于 `app/src/main/assets/database/bus_schedule.db`
- 项目启动时自动复制到应用内部存储
- 包含 31 条时刻表记录和 10 个公交站点数据
- 避免了首次运行时需要手动创建表和插入数据

## 六、ViewModel 从示例数据到数据库数据的切换

### 6.1 修改前（示例数据）
```kotlin
class BusScheduleViewModel: ViewModel() {
    fun getFullSchedule(): Flow<List<BusSchedule>> = flowOf(
        listOf(BusSchedule(1, "Example Street", 0))
    )
}
```

### 6.2 修改后（数据库数据）
```kotlin
class BusScheduleViewModel(
    private val busScheduleDao: BusScheduleDao
) : ViewModel() {
    fun getFullSchedule(): Flow<List<BusSchedule>> = busScheduleDao.getAll()
    fun getScheduleFor(stopName: String): Flow<List<BusSchedule>> =
        busScheduleDao.getByStopName(stopName)

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val database = BusScheduleDatabase.getDatabase(application)
                BusScheduleViewModel(database.busScheduleDao())
            }
        }
    }
}
```

主要变化：
1. 构造函数接收 `BusScheduleDao` 参数
2. ViewModel 通过 DAO 访问数据库
3. `factory` 中创建数据库实例并注入到 ViewModel
4. 删除了 `flowOf()` 示例数据

## 七、Flow<List<BusSchedule>> 的 Compose 使用

`Flow<List<BusSchedule>>` 是 Kotlin 响应式数据流，Compose 页面通过 `collectAsState()` 收集：

```kotlin
val busScheduleViewModel: BusScheduleViewModel = viewModel(factory = BusScheduleViewModel.factory)
val scheduleList by busScheduleViewModel.getFullSchedule().collectAsState()
```

工作原理：
1. ViewModel 提供 `Flow<List<BusSchedule>>`
2. Compose 使用 `collectAsState()` 收集 Flow 的最新值
3. 当数据库数据变化时，Flow 自动推送新数据
4. Compose 自动重组（recomposition）更新 UI

## 八、实验总结

本次实验完成了以下任务：
1. 添加了 Room 依赖和 KSP 编译器依赖
2. 将 `BusSchedule` 转换为 Room Entity，正确映射到 `Schedule` 表
3. 创建了 `BusScheduleDao`，包含完整列表查询和按站点查询
4. 创建了 `BusScheduleDatabase`，通过 `createFromAsset()` 使用预置数据库
5. 更新了 ViewModel，从数据库读取真实数据代替示例数据

实验过程中注意：
- 表名必须与数据库一致（`Schedule`，不是 `BusSchedule`）
- 列名必须使用 `@ColumnInfo` 指定（如 `stop_name`）
- 不要在 Composable 中直接创建数据库，应通过 ViewModel 获取