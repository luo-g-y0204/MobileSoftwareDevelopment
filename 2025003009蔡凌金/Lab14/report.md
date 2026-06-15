# Lab14：使用 Room 完成 Bus Schedule 应用 实验报告
## 一、Entity、DAO、Database 在本实验中的职责
1. **Entity（实体类）**
Entity 是 Room 中的核心组件，作用是将 Kotlin 数据类与数据库中的表进行一一映射。
在本次实验中，BusSchedule 被标记为 @Entity，对应数据库里的 Schedule 表。
它定义了表名、主键、列名，让 Room 能够识别并自动创建对应的表结构，
同时让程序可以用对象的方式操作数据库，而不用写原生 SQL。

2. **DAO（数据访问对象）**
DAO 全称 Data Access Object，专门负责定义数据库的查询、增删改操作。
它是应用与数据库之间的接口，我们只需要调用方法，Room 会自动实现底层查询。
本次实验中 DAO 提供两个核心功能：
- 查询所有公交时刻表
- 根据站点名称查询对应到站时间
所有操作通过 @Query 注解声明 SQL 语句，Room 自动完成执行。

3. **Database（数据库类）**
Database 是整个 Room 数据库的总入口，继承自 RoomDatabase。
它的作用包括：
- 管理数据库实例（单例模式）
- 提供获取 DAO 的方法
- 通过 createFromAsset 加载预置的数据库文件
- 管理数据库版本
它保证整个应用只使用一个数据库实例，避免资源浪费与数据异常。

## 二、BusSchedule 属性与数据库表、列的映射关系
本次实验数据库表名为 Schedule，结构如下：
id INTEGER NOT NULL PRIMARY KEY
stop_name TEXT NOT NULL
arrival_time INTEGER NOT NULL

映射关系如下：
- id → 表中的 id 列，使用 @PrimaryKey 标记主键
- stopName → 表中的 stop_name 列，使用 @ColumnInfo(name = "stop_name") 映射
- arrivalTimeInMillis → 表中的 arrival_time 列，使用 @ColumnInfo(name = "arrival_time") 映射

Room 通过注解自动把数据库下划线命名转为 Kotlin 驼峰命名，
让代码更规范，同时保证与数据库结构完全匹配。

## 三、两条 DAO 查询语句的作用及排序原因
本次实验 DAO 包含两条查询语句：

1. getAll()
SQL：SELECT * FROM Schedule ORDER BY arrival_time ASC
作用：获取数据库中所有公交到站记录，用于首页显示完整公交时刻表。

2. getByStopName(stopName: String)
SQL：SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC
作用：根据传入的站点名称，查询该站所有到站时间，用于详情页面。

两条语句都使用 ORDER BY arrival_time ASC 进行升序排序，
因为 arrival_time 是时间戳，按时间从小到大排序，
可以让公交时刻按“最早到站→最晚到站”正常展示，符合用户使用习惯。

## 四、createFromAsset("database/bus_schedule.db") 的作用
该方法是 Room 提供的非常重要的工具，作用如下：

1. 从 assets 目录中读取已经准备好的 SQLite 数据库文件
2. 在应用第一次运行时，自动将该数据库复制到应用的私有目录
3. 让应用直接使用预置好的表和数据，不需要手动创建表和插入数据
4. 保证应用启动后立刻能读取真实数据

在本次实验中，该方法直接加载 bus_schedule.db，
让程序无需初始化数据，开箱即用，极大简化开发流程。

## 五、ViewModel 如何从示例数据切换为数据库数据
起始代码中 ViewModel 使用 flowOf() 提供写死的假数据：
Example Street | 12:00 AM

改造步骤：
1. 给 ViewModel 增加构造参数 BusScheduleDao
2. 删除原来写死的 flowOf() 示例数据
3. getFullSchedule() 改为调用 dao.getAll()
4. getScheduleFor(stopName) 改为调用 dao.getByStopName(stopName)
5. 通过 ViewModelFactory 传入 Application 并创建数据库实例

最终 ViewModel 不再使用假数据，而是完全从 Room 数据库获取真实数据。

## 六、Flow<List<BusSchedule>> 如何被 Compose 收集并显示
Room 查询返回 Flow 类型，具有以下特点：
- 异步流式数据
- 自动后台线程执行
- 数据变化时自动通知 UI

在 Compose 中：
1. ViewModel 暴露 Flow
2. UI 使用 collectAsState() 收集 Flow
3. 当数据库数据变化时，Flow 自动发射新数据
4. Compose 自动刷新界面显示最新列表

整个过程无需手动处理线程、数据库打开关闭，
完全由 Room + Flow + Compose 自动完成。

## 七、实验中遇到的问题与解决过程
1. 运行后仍然显示 Example Street 示例数据
原因：没有删除 ViewModel 中原来的假数据代码。
解决：清空 flowOf，改为调用 DAO 方法。

2. 数据库表找不到，程序崩溃
原因：@Entity(tableName) 写错，没有写成 Schedule。
解决：修改表名与数据库一致。

3. 列名不匹配，查询为空
原因：stopName 和 arrivalTimeInMillis 没有添加 @ColumnInfo。
解决：添加映射注解，对应 stop_name 和 arrival_time。

4. Room 编译报错，无法生成类
原因：没有添加 ksp 依赖或版本不一致。
解决：在 build.gradle.kts 正确添加 room-runtime、room-ktx、ksp 编译器。

5. 无法加载预置数据库
原因：路径错误，应为 database/bus_schedule.db。
解决：检查文件路径并重新创建数据库实例。

## 实验总结
本次实验成功完成了 Room 数据库的完整使用流程：
- 定义 Entity 映射表
- 编写 DAO 实现查询
- 创建 Database 并加载预置数据库
- ViewModel 从数据库获取数据
- Compose 收集 Flow 展示列表
应用最终可以正常显示所有公交站点与到站时间，功能完整、运行稳定。