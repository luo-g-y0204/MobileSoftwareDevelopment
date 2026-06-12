# Lab14 实验报告：使用 Room 完成 Bus Schedule 应用

## 1. Entity、DAO、Database 的职责说明

本实验中，Room 数据层由三个核心组件构成，各自职责如下：

- **Entity（实体，BusSchedule）**
    负责定义数据库表结构与 Kotlin 数据类之间的映射关系。通过 `@Entity` 注解将 `BusSchedule` 数据类映射到 SQLite 中的 `Schedule` 表，并使用 `@PrimaryKey` 和 `@ColumnInfo` 将属性与表中的列一一对应。Entity 是数据的基本载体，不包含任何业务逻辑。
- **DAO（数据访问对象，BusScheduleDao）**
    负责定义对数据库进行增删改查的接口。本实验只需要查询，因此在 `BusScheduleDao` 接口中使用 `@Query` 注解编写了两条 SQL 查询语句，分别用于获取全部时刻表和按站点名称过滤的时刻表。DAO 的方法返回 `Flow<List<BusSchedule>>`，使得数据变化可以自动通知上层观察者。
- **Database（数据库，BusScheduleDatabase）**
    负责创建和管理 Room 数据库实例。它继承 `RoomDatabase`，使用 `@Database` 注解声明包含的 Entity 和数据库版本，并通过抽象方法暴露 DAO 实例。在伴生对象中实现了单例模式，利用 `Room.databaseBuilder()` 构建数据库，并调用 `createFromAsset()` 从预置的 `bus_schedule.db` 文件填充初始数据。

三者的协作流程为：Database 提供 DAO 实例 → DAO 执行 SQL 返回 Flow → ViewModel 调用 DAO 方法 → Compose UI 收集 Flow 显示数据。

## 2. BusSchedule 属性与 Schedule 表的映射

`BusSchedule` 数据类通过 Room 注解与数据库中已有的 `Schedule` 表建立映射关系：

- `id` 属性使用 `@PrimaryKey` 注解，映射到表的 `id` 列（列名相同，无需额外指定 `@ColumnInfo`）。
- `stopName` 属性使用 `@ColumnInfo(name = "stop_name")` 注解，映射到表的 `stop_name` 列。因为 Kotlin 属性名采用驼峰命名，而数据库列名使用下划线分隔，所以必须通过 `ColumnInfo` 显式指定映射。
- `arrivalTimeInMillis` 属性使用 `@ColumnInfo(name = "arrival_time")` 注解，映射到表的 `arrival_time` 列，同理处理命名风格差异。

最后，通过 `@Entity(tableName = "Schedule")` 指定了映射的目标表名，确保 Room 能够正确识别并匹配预置数据库中的 `Schedule` 表结构。

## 3. DAO 查询语句的作用与排序原因

`BusScheduleDao` 中定义了两条查询：

- `getAll()`：对应 SQL 语句
    `SELECT * FROM Schedule ORDER BY arrival_time ASC`
    查询 `Schedule` 表中所有记录，并按照 `arrival_time` 升序排序。用于首页显示完整的公交时刻表，按到站时间从早到晚排列。
- `getByStopName(stopName: String)`：对应 SQL 语句
    `SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC`
    根据传入的站点名称筛选记录，同样按 `arrival_time` 升序排序。用于站点详情页，只展示该站点的所有到站时间，且按时间顺序排列，方便用户查看下一班车的时间。

两条查询都按 `arrival_time` 排序的原因是：公交时刻表本质上是时间序列数据，用户关注的是一天中按照时间先后发生的到站事件。按时间升序显示，可以让用户快速找到最近的班次，符合实际使用场景的直观逻辑。

## 4. createFromAsset("database/bus_schedule.db") 的作用

`createFromAsset("database/bus_schedule.db")` 是 Room 数据库构建器提供的方法，用于在首次创建数据库时，从应用的 `assets` 目录中复制一个预置的 SQLite 数据库文件作为 Room 数据库的初始数据，而不是执行 `onCreate()` 回调中的建表语句。

具体执行过程为：当应用首次访问 `BusScheduleDatabase` 并触发数据库创建时，Room 会检查 `app/src/main/assets/database/bus_schedule.db` 文件，将其内容拷贝到应用私有数据库目录，同时验证其表结构与 `@Entity` 定义是否一致。如果一致，则直接使用该文件中的现有数据；如果不一致（例如表名或列名不匹配），则会在运行时抛出异常。

本实验中，该机制使得应用可以直接加载预置的 31 条公交时刻数据，而无需编写额外的数据导入代码，简化了开发和测试流程。

## 5. ViewModel 从示例数据切换为数据库数据

修改前的 `BusScheduleViewModel` 使用 `flowOf()` 返回写死的一条示例数据：

kotlin

```
fun getFullSchedule(): Flow<List<BusSchedule>> = flowOf(
    listOf(BusSchedule(1, "Example Street", 0))
)
```



切换到数据库数据的过程中，对 ViewModel 进行了以下关键修改：

1. **引入 DAO 依赖**：在构造函数中添加 `busScheduleDao: BusScheduleDao` 参数，使 ViewModel 持有数据访问对象。
2. **委托查询给 DAO**：将 `getFullSchedule()` 和 `getScheduleFor()` 的实现直接改为调用 DAO 的对应方法，返回 `Flow<List<BusSchedule>>`。
3. **使用自定义 Factory 注入依赖**：由于 ViewModel 需要 `Application` 上下文来获取数据库实例，不能在 Composable 中直接手动创建 ViewModel，因此通过伴生对象实现了 `ViewModelProvider.Factory`。在 `factory` 的 `initializer` 中，利用 `APPLICATION_KEY` 获取 `Application`，再通过 `BusScheduleDatabase.getDatabase(application)` 创建数据库单例，最终将 `database.busScheduleDao()` 注入 ViewModel 构造函数。

这样，所有通过 `getFullSchedule()` 和 `getScheduleFor()` 获取数据的 UI 组件都会自动从数据库读取真实数据，无需更改 Compose 页面代码。

## 6. Flow<List<BusSchedule>> 被 Compose 页面收集和显示

在 Compose UI 中，数据流通过 `collectAsState()` 方法被收集并转换为 Compose 可观察的状态：

- `BusScheduleScreens.kt` 中的 FullScheduleScreen 和 RouteScheduleScreen 分别调用了 `viewModel.getFullSchedule()` 和 `viewModel.getScheduleFor(stopName)`，得到 `Flow<List<BusSchedule>>`。
- 这些 Flow 在 Composable 函数内通过 `.collectAsState(initial = emptyList())` 收集，生成一个 `State<List<BusSchedule>>` 对象。
- 当数据库中的数据因查询条件变化（例如进入站点详情页）而发射新的列表时，Flow 会通知收集者，`collectAsState` 自动更新 `State` 的值，触发 Composable 重组。
- 重组时，`LazyColumn` 根据最新的列表数据重新渲染，从而在界面上显示出对应站点和到站时间。

整个过程是响应式的，开发者只需声明数据来源和界面布局，框架负责在数据变化时自动更新 UI。

## 7. 实验中遇到的问题与解决过程

**问题一：Room 注解无法识别，编译报错“Unresolved reference: Entity”**

- **原因**：最初只添加了 `room-ktx` 和 `room-runtime` 依赖，但遗漏了 KSP 编译器依赖 `ksp("androidx.room:room-compiler:...")`。Room 的注解处理依赖 KSP（或 kapt）在编译时生成实现类。
- **解决**：在 `app/build.gradle.kts` 的 `dependencies` 中补充了 KSP 依赖，并确保项目级 `build.gradle.kts` 中已声明 `com.google.devtools.ksp` 插件。重新同步 Gradle 后编译通过。

**问题二：运行应用时崩溃，提示“Pre-packaged database has an invalid schema”**

- **原因**：`BusSchedule` 实体中的 `@Entity` 注解最初写成了 `@Entity(tableName = "BusSchedule")`，与预置数据库中实际的表名 `Schedule` 不一致。
- **解决**：根据数据库文件结构，将 `tableName` 改为 `"Schedule"`，同时检查 `@ColumnInfo` 中的列名确保与预置数据库的列名完全一致（`stop_name`、`arrival_time`）。修改后错误消失，数据正确加载。

**问题三：首页仍显示“Example Street”示例数据**

- **原因**：ViewModel 中的 `getFullSchedule()` 没有修改成功，或依然保留了 `flowOf()` 调用。经检查发现，在重构过程中曾误保存了一个返回硬编码数据的版本。
- **解决**：彻底删除 `flowOf` 相关代码，确保 `getFullSchedule()` 直接返回 `busScheduleDao.getAll()`。重新运行后首页显示了数据库中的真实时刻表。

通过以上问题的排查和解决，加深了对 Room 依赖配置、数据库 schema 匹配以及 ViewModel 数据流切换的理解。
