# Lab14：使用 Room 完成 Bus Schedule 应用实验报告

## 一、实验目的

本实验在 Bus Schedule 起始项目的基础上，将原来写死在 `BusScheduleViewModel` 中的示例数据替换为 Room 本地数据库数据。通过本实验，掌握 Room 中 `Entity`、`DAO`、`Database` 的基本使用方法，并理解 ViewModel 如何向 Jetpack Compose 页面提供数据库查询结果。

## 二、Entity、DAO、Database 的职责

### 1. Entity

`Entity` 用来描述 Kotlin 数据类与数据库表之间的映射关系。本实验中，`BusSchedule` 被声明为 Room Entity，对应预置数据库中的 `Schedule` 表。Room 会根据 Entity 的注解知道应该从哪张表、哪些列读取数据。

### 2. DAO

`DAO` 是数据访问对象，用来定义数据库操作方法。本实验中创建了 `BusScheduleDao`，其中包含两个查询方法：一个用于查询完整公交时刻表，另一个用于根据站点名称查询该站点的所有到站时间。

### 3. Database

`Database` 是 Room 数据库入口。本实验中创建了 `BusScheduleDatabase`，它继承自 `RoomDatabase`，负责创建数据库实例、提供 DAO，并通过 `createFromAsset()` 使用项目 assets 目录中的预置数据库文件。

## 三、BusSchedule 与 Schedule 表的映射关系

预置数据库中有一张表 `Schedule`，表结构如下：

```sql
CREATE TABLE Schedule(
  id INTEGER NOT NULL,
  stop_name TEXT NOT NULL,
  arrival_time INTEGER NOT NULL,
  PRIMARY KEY (id)
);
```

本实验中的 `BusSchedule` 数据类映射关系如下：

| Kotlin 属性 | 数据库列 | 说明 |
| --- | --- | --- |
| `id` | `id` | 主键，唯一标识一条公交时刻记录 |
| `stopName` | `stop_name` | 公交站点名称 |
| `arrivalTimeInMillis` | `arrival_time` | 到站时间戳 |

核心代码如下：

```kotlin
@Entity(tableName = "Schedule")
data class BusSchedule(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "stop_name") val stopName: String,
    @ColumnInfo(name = "arrival_time") val arrivalTimeInMillis: Int
)
```

这里必须使用 `@Entity(tableName = "Schedule")` 指定表名，否则 Room 默认会使用类名 `BusSchedule` 作为表名，导致无法正确读取预置数据库。

## 四、DAO 查询语句的作用

本实验的 DAO 中定义了两条查询语句：

```kotlin
@Query("SELECT * FROM Schedule ORDER BY arrival_time ASC")
fun getAll(): Flow<List<BusSchedule>>
```

这条语句用于查询完整公交时刻表，并按照 `arrival_time` 从早到晚升序排列。这样首页展示的数据顺序就是公交到站时间顺序。

```kotlin
@Query("SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC")
fun getByStopName(stopName: String): Flow<List<BusSchedule>>
```

这条语句用于查询某个指定站点的所有到站时间。`:stopName` 会使用方法参数 `stopName` 传入的值。它同样按照 `arrival_time` 升序排列，保证详情页中的到站时间从早到晚显示。

之所以需要排序，是因为数据库中的记录虽然有固定 id，但页面展示公交时刻表时更关心时间顺序，而不是记录编号顺序。

## 五、createFromAsset() 的作用

本实验中数据库文件已经放在：

```text
app/src/main/assets/database/bus_schedule.db
```

在创建 Room 数据库时使用：

```kotlin
.createFromAsset("database/bus_schedule.db")
```

它的作用是让 Room 第一次创建数据库时，从 assets 目录中的 `bus_schedule.db` 复制并初始化数据。这样应用启动后可以直接读取预置的公交时刻数据，而不需要在代码中手动插入数据。

## 六、ViewModel 如何从示例数据切换为数据库数据

起始项目中的 `BusScheduleViewModel` 使用 `flowOf()` 返回写死的示例数据，所以应用只能显示：

```text
Example Street | 12:00 AM
```

修改后，`BusScheduleViewModel` 不再自己创建示例数据，而是通过构造函数接收 `BusScheduleDao`：

```kotlin
class BusScheduleViewModel(
    private val busScheduleDao: BusScheduleDao
) : ViewModel()
```

然后两个方法直接调用 DAO 查询数据库：

```kotlin
fun getFullSchedule(): Flow<List<BusSchedule>> = busScheduleDao.getAll()

fun getScheduleFor(stopName: String): Flow<List<BusSchedule>> =
    busScheduleDao.getByStopName(stopName)
```

在 `factory` 中，通过 `APPLICATION_KEY` 获取 Application Context，再创建数据库实例：

```kotlin
val application = checkNotNull(this[APPLICATION_KEY])
val database = BusScheduleDatabase.getDatabase(application)
BusScheduleViewModel(database.busScheduleDao())
```

这样 ViewModel 就完成了从“写死示例数据”到“读取 Room 数据库数据”的切换。

## 七、Flow<List<BusSchedule>> 如何被 Compose 页面收集并显示

DAO 返回的是 `Flow<List<BusSchedule>>`。Flow 可以在数据变化时持续发出新的数据列表，适合配合 Room 和 Compose 使用。

在 Compose 页面中，起始项目已经使用：

```kotlin
val fullSchedule by viewModel.getFullSchedule().collectAsState(emptyList())
```

这行代码会把 Flow 收集为 Compose 的 State。当数据库查询结果返回后，`fullSchedule` 会更新，Compose 页面会自动重新组合并显示列表。

在站点详情页中，也使用类似方式收集指定站点的数据：

```kotlin
val routeSchedule by viewModel.getScheduleFor(stopName).collectAsState(emptyList())
```

因此，当用户点击某个站点后，页面会根据站点名称查询数据库，并只显示该站点对应的到站时间。

## 八、实验中遇到的问题与解决过程

### 问题一：应用只显示 Example Street

原因是 `BusScheduleViewModel` 仍然使用 `flowOf()` 返回写死数据。

解决方法：删除示例数据，改为通过 `BusScheduleDao` 查询数据库。

### 问题二：Room 无法匹配预置数据库表

如果 Entity 没有指定表名，Room 默认会把数据类名当作表名，导致找不到数据库中的 `Schedule` 表。

解决方法：在 `BusSchedule` 上添加：

```kotlin
@Entity(tableName = "Schedule")
```

### 问题三：数据库字段名和 Kotlin 属性名不一致

数据库中字段名是 `stop_name` 和 `arrival_time`，而 Kotlin 属性名是 `stopName` 和 `arrivalTimeInMillis`。

解决方法：使用 `@ColumnInfo` 显式指定列名：

```kotlin
@ColumnInfo(name = "stop_name")
val stopName: String

@ColumnInfo(name = "arrival_time")
val arrivalTimeInMillis: Int
```

### 问题四：需要从预置数据库读取数据

如果只创建 RoomDatabase，而没有指定预置数据库来源，应用中不会自动拥有 `bus_schedule.db` 的数据。

解决方法：使用：

```kotlin
.createFromAsset("database/bus_schedule.db")
```

让 Room 从 assets 目录复制数据库文件。

## 九、实验总结

本实验完成了 Bus Schedule 应用的数据层改造。通过添加 Room 依赖、声明 Entity、编写 DAO、创建 RoomDatabase，并在 ViewModel 中调用 DAO，应用可以从预置 SQLite 数据库中读取真实公交时刻数据。

本实验的关键是保证 Entity 与数据库表结构完全一致，包括表名、字段名、主键和字段类型。同时，ViewModel 不应该再返回写死的示例数据，而应该作为 UI 与数据层之间的桥梁，把 DAO 返回的 `Flow<List<BusSchedule>>` 提供给 Compose 页面。Compose 页面通过 `collectAsState()` 收集 Flow 后，可以自动刷新界面并显示数据库中的公交时刻表。
