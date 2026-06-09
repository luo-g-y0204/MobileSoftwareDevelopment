# Lab14：使用 Room 完成 Bus Schedule 应用 实验报告

一、Entity、DAO、Database 在本实验中的职责
1. Entity（实体类）
Entity 是 Room 框架的核心组件，主要作用是将 Kotlin 数据类与 SQLite 数据库数据表建立双向映射关系。在本次实验中，被 @Entity 修饰的 BusSchedule 类，对应数据库当中的 Schedule 数据表。它负责定义数据表的表名、主键、字段列等核心结构，让 Room 能够自动识别并创建对应的数据表。开发者可以通过操作实体对象完成数据库交互，摆脱原生 SQL 语句的繁琐编写。
2. DAO（数据访问对象）
DAO 的全称为数据访问对象，是专门用于定义数据库增删改查操作的接口，也是应用程序与本地数据库交互的核心通道。开发者只需通过注解声明对应的 SQL 逻辑，Room 会自动封装底层执行代码。本次实验中 DAO 实现了两大核心功能：查询全部公交时刻表数据、根据站点名称匹配对应的到站记录，支撑页面的数据展示功能。
3. Database（数据库类）
Database 继承自 RoomDatabase，是整个 Room 数据库的统一入口与管理核心。其主要职责包含：通过单例模式统一管理数据库实例，避免多实例造成的数据异常和资源浪费；对外提供 DAO 实例获取方法；借助对应方法加载项目预置数据库文件；同时负责数据库的版本管理，保障数据库稳定运行。
二、BusSchedule 属性与数据库表、列的映射关系
本次实验所用的数据表名为 Schedule，数据表结构包含三列：id 整型主键、stop_name 文本类型站点名、arrival_time 整型时间戳，所有字段均设置为非空。
代码属性与数据表列的详细映射关系如下：
- id 对应数据表 id 列，通过 @PrimaryKey 注解设为数据表唯一主键。
- stopName 对应数据表 stop_name 列，依靠 @ColumnInfo(name = "stop_name") 完成字段映射。
- arrivalTimeInMillis 对应数据表 arrival_time 列，通过 @ColumnInfo(name = "arrival_time") 完成映射绑定。
Room 通过注解适配数据库下划线命名与 Kotlin 驼峰命名规范，既保证代码规范性，又实现代码与数据库结构的精准匹配。
三、两条 DAO 查询语句的作用及排序原因
本次实验 DAO 中定义了两条核心查询语句，具体作用与排序逻辑如下：
1. getAll()
SQL：SELECT * FROM Schedule ORDER BY arrival_time ASC
作用：查询数据库内所有公交到站数据，为应用首页提供完整的公交时刻表展示内容。
2. getByStopName(stopName: String)
SQL：SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC
作用：根据传入的站点名称，精准检索该站点的所有到站记录，用于对应详情页面的数据渲染。
两条查询语句均采用 arrival_time 升序排序。由于该字段存储的是时间戳，从小到大排序可将公交到站时间按早到晚有序展示，贴合用户查看时刻表的使用习惯，提升页面展示逻辑性。
四、createFromAsset("database/bus_schedule.db") 的作用
该方法是 Room 适配预置数据库的核心工具方法，具体作用如下：
一是读取项目 assets 资源目录下提前配置好的 SQLite 数据库文件；二是在应用首次启动时，自动将预置数据库复制到应用私有目录，完成数据库初始化；三是无需手动编写建表、插数等初始化 SQL 代码，直接复用现成的表结构与数据；四是让应用启动即可加载有效数据，大幅简化数据库开发流程，提升开发效率。
五、ViewModel 如何从示例数据切换为数据库数据
项目初始 ViewModel 通过 flowOf() 写入固定的模拟示例数据，无法加载真实业务数据。本次实验通过分步改造，完成数据源的切换，具体步骤如下：
第一，为 ViewModel 新增 BusScheduleDao 构造参数，使其具备调用数据库操作的能力；第二，彻底删除原有 flowOf() 定义的假数据代码；第三，将获取全量时刻表的方法对接 dao.getAll()，将按站点查询的方法对接 dao.getByStopName()；第四，通过 ViewModelFactory 绑定应用上下文，完成数据库实例的创建与注入。改造后 ViewModel 完全依托 Room 数据库获取真实数据。
六、Flow<List<BusSchedule>> 如何被 Compose 收集并显示
Room 数据库查询结果默认返回 Flow 流式数据，具备异步执行、自动监听数据变更、不阻塞主线程的特性。
Compose 页面的数据展示流程为：ViewModel 对外暴露 Flow 类型数据源，UI 层通过 collectAsState() 方法订阅收集流式数据。当数据库数据发生变动时，Flow 会自动推送最新数据，触发 Compose 重组 UI，无需手动处理线程切换和页面刷新，实现数据实时更新展示。
七、实验中遇到的问题与解决过程
1. 运行后仍然展示默认示例假数据
原因：未完全清除 ViewModel 中预设的模拟数据代码，数据源未成功切换至数据库。
解决：删除所有 flowOf 模拟数据代码，将方法返回值替换为 DAO 数据库查询数据流。
2. 程序崩溃，无法识别数据库表
原因：Entity 实体类的表名注解与预置数据库表名不匹配，导致 Room 无法识别数据表。
解决：修改 @Entity 注解内的表名参数，保证与 Schedule 数据表名完全一致。
3. 字段匹配异常，查询数据为空
原因：实体类属性未添加 @ColumnInfo 映射注解，代码字段与数据库列名无法对应。
解决：为所有自定义命名字段补充列名映射注解，完成代码与数据库字段绑定。
4. Room 编译失败，无法生成数据库类
原因：项目缺失 Room 相关依赖，或 ksp 编译插件未配置、版本不兼容。
解决：在 Gradle 配置文件中补全 Room 核心依赖与 ksp 编译器配置，同步项目后重新编译。
5. 无法正常加载预置数据库文件
原因：assets 目录下数据库文件路径填写错误，程序无法检索到目标 db 文件。
解决：核对文件路径层级，修正为标准路径 database/bus_schedule.db，重新初始化数据库实例。
实验总结
本次实验完整实操了 Room 本地数据库的开发全流程，熟练掌握了 Entity 实体映射、DAO 数据操作、Database 数据库配置的核心原理与使用方法。通过加载预置数据库替换静态模拟数据，结合 ViewModel 组件与 Flow 流式数据，实现了异步数据获取与实时 UI 更新，最终通过 Compose 完成公交时刻表的完整展示。本次实验不仅巩固了 Android 本地数据库开发知识，也深入理解了 Jetpack 组件与声明式 UI 的协同开发逻辑，项目功能运行正常、效果达标。