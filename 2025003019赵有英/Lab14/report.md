# Lab14: 使用 Room 完成 Bus Schedule 应用实验报告
## 1. Entity、DAO、Database 三者在本实验中的职责说明
- **Entity（BusSchedule.kt）**：数据模型层，负责将 Kotlin 数据类映射到 SQLite 数据库表，定义了表结构和字段对应关系。
- **DAO（BusScheduleDao.kt）**：数据访问层，定义了操作数据库的接口方法和对应的 SQL 查询语句，是应用与数据库交互的桥梁。
- **Database（BusScheduleDatabase.kt）**：数据库管理层，负责创建和管理数据库实例，提供 DAO 对象，并实现了从 assets 预置数据库初始化数据的功能。

## 2. BusSchedule 属性如何映射到 Schedule 表和列
- `id` 属性通过 `@PrimaryKey` 映射到 `Schedule` 表的 `id` 列（主键）
- `stopName` 属性通过 `@ColumnInfo(name = "stop_name")` 映射到 `stop_name` 列
- `arrivalTimeInMillis` 属性通过 `@ColumnInfo(name = "arrival_time")` 映射到 `arrival_time` 列
- 整个类通过 `@Entity(tableName = "Schedule")` 映射到 `Schedule` 表

## 3. 两条 DAO 查询语句的作用，以及为什么需要按 arrival_time 排序
- `getAll()`：查询 `Schedule` 表中的所有记录，返回完整的公交时刻表
- `getByStopName(stopName: String)`：根据站点名称查询该站点的所有到站时间记录
- 按 `arrival_time ASC` 排序的原因：公交时刻表需要按时间先后顺序展示，方便用户查看下一班车的时间。

## 4. createFromAsset("database/bus_schedule.db") 的作用
- 从应用的 `assets/database/` 文件夹中复制预置的 `bus_schedule.db` 数据库文件到设备的应用数据库目录
- 避免在应用首次启动时通过代码创建表和插入数据，提高了初始化效率
- 适用于需要预装大量静态数据的场景（如公交时刻表、词典等）

## 5. ViewModel 如何从示例数据切换为数据库数据
- 删除了原来使用 `flowOf()` 硬编码的示例数据
- 修改 ViewModel 构造函数，接收 `BusScheduleDao` 作为依赖
- 将 `getFullSchedule()` 和 `getScheduleFor()` 方法改为直接调用 DAO 的对应方法
- 通过 ViewModel Factory 从 Application 上下文获取数据库实例，并注入到 ViewModel 中

## 6. Flow<List<BusSchedule>> 如何被 Compose 页面收集并显示
- Compose UI 通过 `viewModel.getFullSchedule().collectAsStateWithLifecycle()` 收集 Flow 数据
- `collectAsStateWithLifecycle()` 会将 Flow 转换为 Compose 可观察的 State
- 当数据库数据变化时，State 会自动更新，触发 Composable 重组
- 重组时，`LazyColumn` 会根据最新的 List 数据重新渲染列表项

## 7. 实验中遇到的问题与解决过程
### 问题1：应用运行后仍然显示 Example Street
- **原因**：ViewModel 没有正确替换为数据库查询，还在使用原来的示例数据
- **解决**：完全替换 BusScheduleViewModel.kt 文件，确保删除了所有 flowOf() 代码

### 问题2：编译报错：Unresolved reference: APPLICATION_KEY
- **原因**：缺少必要的 import 语句
- **解决**：添加 import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY

### 问题3：应用启动崩溃，提示 "table Schedule not found"
- **原因**：@Entity 注解中的 tableName 写错了（写成了 BusSchedule 而不是 Schedule）
- **解决**：修改为 @Entity(tableName = "Schedule")