# Lab14 实验报告：使用 Room 完成 Bus Schedule 应用

## 一、实验目标

本次实验基于 Bus Schedule 起始项目，将原来写死在 ViewModel 中的示例数据替换为 Room 本地数据库数据。完成后，应用首页可以显示完整公交时刻表，点击某个站点后可以进入详情页，只查看该站点的到站时间。

## 二、Entity、DAO、Database 的职责

`Entity` 用来描述 Kotlin 数据类和数据库表之间的对应关系。本实验中 `BusSchedule` 对应数据库中的 `Schedule` 表。

`DAO` 用来集中管理数据库查询语句。本实验中 `BusScheduleDao` 提供完整时刻表查询和按站点名称查询两个方法。

`Database` 是 Room 数据库入口。本实验中 `BusScheduleDatabase` 继承 `RoomDatabase`，负责创建数据库实例，并提供 `busScheduleDao()` 给 ViewModel 调用。

## 三、BusSchedule 与 Schedule 表的映射

数据库中表名为 `Schedule`，包含三列：`id`、`stop_name` 和 `arrival_time`。

在 `BusSchedule.kt` 中，使用 `@Entity(tableName = "Schedule")` 指定表名；`id` 使用 `@PrimaryKey` 作为主键；`stopName` 使用 `@ColumnInfo(name = "stop_name")` 映射数据库列；`arrivalTimeInMillis` 使用 `@ColumnInfo(name = "arrival_time")` 映射到到站时间列。

## 四、DAO 查询语句说明

`getAll()` 使用：

```sql
SELECT * FROM Schedule ORDER BY arrival_time ASC
```

它用于获取完整时刻表，并按到站时间从早到晚显示。

`getByStopName(stopName)` 使用：

```sql
SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC
```

它用于获取某个指定站点的所有到站时间。两个查询都按 `arrival_time` 升序排列，是为了让列表显示顺序符合公交时刻表的习惯。

## 五、createFromAsset 的作用

`createFromAsset("database/bus_schedule.db")` 表示 Room 创建数据库时，从 `app/src/main/assets/database/bus_schedule.db` 这个预置数据库文件复制数据。这样应用第一次运行时就已经有公交时刻数据，不需要用户手动输入，也不需要从网络下载。

## 六、ViewModel 的修改

原来的 `BusScheduleViewModel` 使用 `flowOf()` 返回 `Example Street` 示例数据。本次修改后，ViewModel 的构造函数接收 `BusScheduleDao`，`getFullSchedule()` 直接调用 `busScheduleDao.getAll()`，`getScheduleFor(stopName)` 调用 `busScheduleDao.getByStopName(stopName)`。

在 `factory` 中，通过 `APPLICATION_KEY` 获取 Application Context，再调用 `BusScheduleDatabase.getDatabase(application)` 创建数据库实例，最后把 DAO 传入 ViewModel。

## 七、Flow 如何被 Compose 页面收集并显示

DAO 方法返回的是 `Flow<List<BusSchedule>>`。在 Compose 页面中，起始项目已经使用 `collectAsState(emptyList())` 收集 Flow。数据库数据变化时，Flow 会发出新的列表，Compose 会自动重新组合并更新界面。

## 八、运行结果

首页运行后不再显示 `Example Street`，而是显示数据库中的完整公交时刻表。点击 `Main Street`、`Park Street` 等站点后，可以进入对应站点的详情页，只显示该站点的到站时间。

请将运行后的两张截图放入提交文件夹：

- `screenshot_full_schedule.png`：完整时刻表首页
- `screenshot_route_schedule.png`：某个站点的详情页

## 九、遇到的问题与解决过程

1. 表名和列名必须和预置数据库保持一致。解决方法是在 Entity 中使用 `@Entity(tableName = "Schedule")` 和 `@ColumnInfo` 明确指定列名。
2. 如果仍然显示 `Example Street`，说明 ViewModel 还在使用示例数据。解决方法是删除 `flowOf()` 示例数据，改为调用 DAO。
3. Room 需要 KSP 编译器依赖。解决方法是在 app 模块 Gradle 文件中加入 `ksp("androidx.room:room-compiler:...")`。
4. 预置数据库必须放在 `assets/database/bus_schedule.db` 下，并在 Database 中使用 `createFromAsset("database/bus_schedule.db")` 加载。
