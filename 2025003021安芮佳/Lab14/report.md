# Lab14 实验报告
## 一、实验基本信息
1. **实验名称**：使用 Room 完成 Bus Schedule 公交时刻表应用
2. **实验目的**：掌握 Android Room 持久化库的使用，完成实体类、DAO、数据库的创建，读取 `assets` 预置 SQLite 数据库数据，并结合 ViewModel、Flow 与 Jetpack Compose 完成数据展示。
3. **实验环境**：Android Studio、Kotlin、Jetpack Compose、Room 2.7.0、Android 模拟器

## 二、核心组件职责说明
### 1. Entity（BusSchedule）
`BusSchedule` 是 Room 实体类，作用是**映射 SQLite 数据库中的数据表**，将 Kotlin 数据类与数据库 `Schedule` 表建立一一对应关系，定义表结构、字段类型、主键和字段别名，是 Room 实现 ORM（对象关系映射）的基础组件。应用通过该类实现数据库行数据与 Kotlin 对象的相互转换。

### 2. DAO（BusScheduleDao）
DAO（数据访问对象）是 Room 中负责**定义数据库读写操作**的接口，通过注解编写 SQL 查询语句，封装数据查询逻辑。本实验中仅包含查询操作，对外提供标准化的数据获取方法，隔离上层业务代码与原生 SQL，统一数据访问入口。

### 3. Database（BusScheduleDatabase）
`BusScheduleDatabase` 是 Room 数据库主类，继承自 `RoomDatabase`，作为整个数据库的入口。该类使用单例模式保证全局仅有一个数据库实例，避免多次创建引发内存泄漏与线程安全问题；同时管理数据库版本、关联所有 Entity，并对外提供 DAO 实例，是连接 Entity、DAO 与应用的核心容器。

## 三、BusSchedule 实体类与数据表映射关系
预置数据库中存在名为 `Schedule` 的数据表，`BusSchedule` 通过 Room 注解完成精准映射，具体对应规则如下：
1. **表名映射**：使用 `@Entity(tableName = "Schedule")` 指定当前实体类对应数据库中的 `Schedule` 表，表名必须与预置数据库完全一致。
2. **主键映射**：`val id: Int` 标注 `@PrimaryKey`，对应数据表 `id` 字段，作为表的唯一主键，非空且唯一。
3. **普通字段映射**
   - Kotlin 属性 `stopName`：通过 `@ColumnInfo(name = "stop_name")` 映射数据库列 `stop_name`，存储公交站点名称；
   - Kotlin 属性 `arrivalTimeInMillis`：通过 `@ColumnInfo(name = "arrival_time")` 映射数据库列 `arrival_time`，存储到站时间时间戳。

完整映射代码：
```kotlin
@Entity(tableName = "Schedule")
data class BusSchedule(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "stop_name")
    val stopName: String,
    @ColumnInfo(name = "arrival_time")
    val arrivalTimeInMillis: Int
)
```

## 四、DAO 查询语句解析
`BusScheduleDao` 中定义两条查询方法，均返回 `Flow<List<BusSchedule>>`，支持数据实时监听，两条 SQL 语句功能与排序原因如下：
### 1. 查询全部时刻表
```sql
SELECT * FROM Schedule ORDER BY arrival_time ASC
```
- **作用**：查询 `Schedule` 表中**所有公交时刻表数据**，用于应用首页展示完整列表。
- **排序原因**：`arrival_time` 为到站时间戳，`ASC` 代表升序排序，可让数据按照**到站时间从早到晚**展示，符合用户查看公交时刻表的使用习惯。

### 2. 根据站点名称查询时刻表
```sql
SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC
```
- **作用**：通过参数 `stopName` 匹配数据库 `stop_name` 字段，**筛选出指定公交站点的所有到站记录**，用于站点详情页展示。
- **排序原因**：和全量查询一致，对筛选后的站点数据按到站时间升序排列，保证详情页时间顺序规整。

## 五、createFromAsset 方法作用
`createFromAsset("database/bus_schedule.db")` 是 Room 提供的预置数据库初始化方法，核心作用如下：
1. **加载预置数据库**：应用首次创建 Room 数据库时，自动读取项目 `src/main/assets/database/` 目录下的 `bus_schedule.db` 文件，将这个预先填充好数据的 SQLite 文件作为 Room 数据库的初始数据源。
2. **避免手动造数**：无需在代码中通过 `@Insert` 等方式批量插入测试数据，直接复用已有 SQLite 数据库中的 31 条时刻表数据与 10 个公交站点数据。
3. **初始化时机**：仅在数据库实例**第一次创建**时执行，后续应用重启不会重复加载，保证数据稳定性。

## 六、ViewModel 数据切换逻辑
起始项目中 ViewModel 通过 `flowOf()` 返回硬编码的示例数据（Example Street），本实验将其改造为从 Room 数据库读取真实数据，改造流程如下：
1. **修改构造函数**：为 `BusScheduleViewModel` 添加 `BusScheduleDao` 作为构造参数，实现 ViewModel 依赖 DAO 获取数据。
2. **替换数据来源**：删除原有写死的示例数据流，`getFullSchedule()` 直接调用 `dao.getAll()`、`getScheduleFor()` 直接调用 `dao.getByStopName()`，将数据来源切换为数据库查询结果。
3. **重构 ViewModel 工厂**：通过 `ViewModelProvider.Factory` 获取全局 `Application` 上下文，调用 `BusScheduleDatabase.getDatabase()` 获取单例数据库实例，再通过数据库拿到 DAO，最终实例化 ViewModel，保证上下文安全（使用应用上下文，避免页面销毁引发内存泄漏）。
4. **数据流转**：ViewModel 作为中间层，接收 DAO 返回的 `Flow` 数据流，对外暴露给 Compose UI，实现数据解耦。

## 七、Flow 数据流在 Compose 中的使用
`Flow<List<BusSchedule>>` 是 Kotlin 协程的数据流，具备**可观察、可实时更新**的特性，在 Compose 中的收集与展示流程：
1. **数据流来源**：DAO 的查询方法返回 `Flow`，当数据库数据发生变化时，`Flow` 会自动发射新数据。
2. **Compose 收集数据**：在 Compose 可组合函数中，使用 `collectAsState()` 函数收集 ViewModel 提供的 `Flow`，将数据流转换为 Compose 可感知的状态 `State`。
3. **界面自动刷新**：`collectAsState` 具备生命周期感知能力，当 `Flow` 推送新数据时，Compose 会自动重组对应 UI 组件，将最新的公交时刻表列表渲染到页面。
4. **生命周期管理**：页面销毁时自动停止数据流收集，避免协程泄漏，适配 Android 页面生命周期。

简单流程总结：`数据库数据变更 → DAO 的 Flow 发射新数据 → ViewModel 转发数据流 → Compose collectAsState 监听 → UI 自动刷新`。

## 八、实验问题与解决过程
### 问题1：编译报错，Room 注解无法识别
- **现象**：编写 Entity、DAO 后，Android Studio 提示 Room 相关注解找不到，项目编译失败。
- **原因**：未正确配置 Room 依赖与 KSP 编译器。
- **解决**：在项目级 `build.gradle.kts` 中添加 Room 版本号 `room_version = 2.7.0`；在 App 模块 `build.gradle.kts` 中添加 `room-runtime`、`room-ktx` 依赖与 `ksp` 编译器依赖，同步 Gradle 后编译正常。

### 问题2：运行后页面仍显示示例数据，未加载数据库内容
- **现象**：应用启动后依旧展示 `Example Street | 12:00 AM`，数据库数据无展示。
- **原因**：ViewModel 未彻底删除原有 `flowOf()` 示例数据，未对接 DAO。
- **解决**：删除 ViewModel 中硬编码的示例数据流，让两个查询方法直接调用 DAO 接口，重新运行后正常加载数据库数据。

### 问题3：查询结果为空，页面无任何数据展示
- **现象**：依赖、代码均无报错，但列表空白。
- **原因**：Entity 的 `tableName` 或 DAO 中 SQL 语句的表名/列名与预置数据库不匹配（大小写、下划线错误）。
- **解决**：核对预置数据库表名为 `Schedule`，列名为 `stop_name`、`arrival_time`，修正注解与 SQL 语句中的名称，保证完全一致，数据正常展示。

### 问题4：应用启动崩溃，提示数据表不存在
- **现象**：打开应用立即闪退，日志提示 `no such table: Schedule`。
- **原因**：`createFromAsset` 路径填写错误，无法加载 `assets` 下的预置数据库文件。
- **解决**：确认数据库文件路径为 `assets/database/bus_schedule.db`，修正方法参数为 `database/bus_schedule.db`，重启应用后正常初始化数据库。

## 九、实验运行结果验证
1. **首页列表**：成功加载预置数据库中 31 条公交时刻表数据，按到站时间升序排列，不再显示示例数据。
2. **站点详情页**：点击任意站点（如 Main Street），跳转至详情页，仅展示该站点的所有到站记录，时间顺序正确。
3. **页面导航**：详情页顶部返回按钮可正常返回首页，导航功能正常。
4. **稳定性验证**：旋转屏幕、重启应用后，数据仍正常加载；浅色/深色模式下 UI 展示无异常。

## 十、实验总结
本次实验完整实践了 Android Room 数据库的核心使用流程，掌握了 Entity、DAO、RoomDatabase 三大核心组件的分工与协作，理解了 ORM 思想在 Android 本地持久化中的应用。同时结合 Kotlin Flow、ViewModel 与 Jetpack Compose，实现了**数据库层 → 逻辑层 → UI 层**的标准数据流转架构。

通过本次实验，明确了 Room 相比原生 SQLite 的优势：简化数据库操作、避免原生 SQL 模板代码、结合协程与 Flow 实现异步查询和数据自动刷新，且单例数据库设计有效规避了内存泄漏与线程安全问题。实验过程中遇到的名称匹配、依赖配置、文件路径等问题，也加深了对 Room 细节规则与 Android 资源文件使用规范的理解。