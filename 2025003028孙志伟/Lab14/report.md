# Lab14 基于Room的公交时刻表应用实验报告
## 一、实验概述
本次 Lab14 实验基于 Android Jetpack Compose 公交时刻表项目，核心目标是引入 Room 持久化数据库框架，替换项目中原有的静态模拟数据，读取 assets 目录下预置的 SQLite 公交数据库，实现真实公交站点、到站时间数据的加载与展示。通过本次实验，掌握 Room 核心组件 Entity、DAO、Database 的使用方式，理解 Android 数据层、ViewModel、Compose UI 的数据流转机制。

## 二、核心组件职责说明
### 1. Entity（BusSchedule）
Entity 是 Room 的数据库实体类，作用是映射 SQLite 数据库的数据表。本实验中 BusSchedule 数据类通过 Room 注解与预置数据库中的 Schedule 表一一绑定，定义表结构、主键、字段映射关系，是数据库数据与 Kotlin 实体对象的转换载体，用于封装单条公交时刻表数据。

### 2. DAO（BusScheduleDao）
DAO（数据访问对象）是 Room 用于操作数据库的核心接口，专门用于定义数据库增删改查查询方法。本实验中通过注解编写原生 SQL 查询语句，提供获取全部时刻表、根据站点名称筛选时刻表的能力，是应用访问数据库数据的唯一入口，隔离了底层 SQL 操作与上层业务逻辑。

### 3. Database（BusScheduleDatabase）
Database 是 Room 数据库的核心管理类，继承自 RoomDatabase。其职责为：声明数据库关联的 Entity 实体、指定数据库版本、提供 DAO 实例、通过单例模式全局唯一管理数据库实例，同时加载 assets 下的预置数据库文件，完成数据库的初始化与全局调度。

## 三、实体类与数据库表映射关系
本次实验预置数据库表名为 Schedule，通过 Room 注解完成 Kotlin 实体属性与数据库字段的精准映射，具体对应关系如下：
- 类表绑定：通过 `@Entity(tableName = "Schedule")` 将 BusSchedule 类绑定数据库 Schedule 数据表。
- 主键映射：Kotlin 属性 id，通过 `@PrimaryKey` 注解对应数据库主键字段 id，为非空整型，唯一标识每条时刻表数据。
- 站点名称映射：Kotlin 属性 stopName，通过 `@ColumnInfo(name = "stop_name")` 映射数据库下划线命名字段 stop_name，存储公交站点名称。
- 时间戳映射：Kotlin 属性 arrivalTimeInMillis，通过 `@ColumnInfo(name = "arrival_time")` 映射数据库字段 arrival_time，存储 Unix 到站时间戳。

通过上述注解，解决了 Kotlin 驼峰命名与数据库下划线命名的字段差异，保证数据读取精准匹配。

## 四、DAO 查询语句作用与排序原因
### 1. getAll() 查询语句
SQL：`SELECT * FROM Schedule ORDER BY arrival_time ASC`
作用：查询 Schedule 数据表中所有的公交时刻表数据，用于首页展示完整的公交时刻列表。

### 2. getByStopName() 查询语句
SQL：`SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC`
作用：根据传入的站点名称参数，精准筛选对应站点的所有到站时刻数据，用于站点详情页的专属数据展示。

### 3. 按 arrival_time 升序排序的原因
数据库中原始数据的存储顺序无序，而公交时刻表的核心展示逻辑是按到站时间从早到晚排序。通过 arrival_time ASC 升序排序，可保证首页列表和站点详情页的时刻数据均按照时间顺序正常展示，符合用户查看公交时刻表的使用习惯，同时统一 UI 展示逻辑，避免数据顺序混乱。

## 五、createFromAsset() 方法作用
`createFromAsset("database/bus_schedule.db")` 是 Room 提供的预置数据库初始化方法，核心作用是：在应用首次启动时，加载项目 assets 目录下预先创建好的 bus_schedule.db SQLite 数据库文件，初始化本地 Room 数据库。

常规 Room 数据库为空白数据库，需要手动插入数据；而本实验通过该方法，直接复用已包含 31 条时刻数据、10 个公交站点的成熟数据库文件，无需手动创建表、插入数据，简化开发流程，同时保证测试数据的完整性和统一性，是本地预置固定数据场景的常用方案。

## 六、ViewModel 数据切换逻辑
项目初始状态下，ViewModel 通过`flowOf()` 硬编码模拟静态数据，页面仅展示单条示例数据。本次实验完成了从模拟数据到真实数据库数据的完整切换，具体流程如下：
1. 移除模拟数据：删除 ViewModel 中所有`flowOf()` 生成的静态示例数据流，去除硬编码数据逻辑。
2. 注入 DAO 依赖：修改 BusScheduleViewModel 构造函数，传入 BusScheduleDao 对象，让 ViewModel 具备调用数据库查询方法的能力。
3. 重构数据方法：`getFullSchedule()` 直接返回 DAO 的 `getAll()` 数据流，`getScheduleFor()` 返回 DAO 的站点筛选数据流。
4. 配置 ViewModel 工厂：通过 ViewModel 工厂类，获取应用全局上下文，初始化 Room 数据库实例，绑定 DAO 并注入 ViewModel，完成数据层与业务层的关联。

改造后 ViewModel 不再持有静态数据，完全依赖 Room 数据库提供真实数据。

## 七、Flow 数据在 Compose 中的展示原理
本次实验中数据库查询返回 `Flow<List<BusSchedule>>` 数据流，实现了 Compose UI 的响应式更新，核心流转逻辑如下：
1. 数据层推送数据流：Room 结合 Kotlin Flow 特性，当本地数据库数据发生变化时，会自动发射最新的 `List<BusSchedule>` 数据流，且 Flow 为冷数据流，仅在页面订阅时才执行查询。
2. ViewModel 中转数据：ViewModel 接收 DAO 推送的 Flow 数据流，对外暴露统一的数据获取方法，隔离UI与数据层，保证数据生命周期安全。
3. Compose 收集并渲染数据：Compose 页面通过 `collectAsState()` 函数订阅 ViewModel 中的 Flow 数据流，将流式数据转换为 Compose 可感知的状态数据。
4. UI 自动刷新：当数据库数据更新或页面重建（如屏幕旋转）时，Flow 会推送最新数据，Compose 状态自动更新，触发页面重组，实时展示最新的公交时刻表数据。

该机制实现了数据驱动 UI，无需手动刷新页面，适配 Android 页面生命周期变化。

## 八、实验问题与解决过程
### 问题1：运行应用仍显示默认示例数据，无数据库真实数据
**原因**：ViewModel 未完全删除模拟数据逻辑，或未正确绑定数据库 DAO，仍然返回 flowOf 静态数据。
**解决**：彻底删除 ViewModel 中所有模拟数据流代码，检查 ViewModel 工厂类是否正确初始化数据库、获取 DAO 实例，重新同步代码并运行，成功加载数据库真实数据。

### 问题2：应用启动崩溃，提示数据表不存在
**原因**：Entity 注解中表名编写错误，未匹配数据库原生的 Schedule 表名，Room 无法识别数据表。
**解决**：核对数据库表结构，修正 `@Entity` 注解的 tableName 属性为 Schedule，严格匹配原生数据库表名，问题解决。

### 问题3：查询数据为空，页面无数据展示
**原因**：实体类字段映射错误，驼峰属性与数据库下划线字段未通过 ColumnInfo 绑定，导致字段匹配失败。
**解决**：为 stopName、arrivalTimeInMillis 属性添加正确的 `@ColumnInfo` 映射注解，对应 stop_name、arrival_time 数据库字段，重新编译后数据正常加载。

### 问题4：Room 编译报错，无法生成数据库类
**原因**：未正确配置 KSP 编译器依赖，Room 注解无法被解析。
**解决**：核对项目级和模块级 Gradle 配置，添加正确的 Room 版本号、runtime、ktx、ksp 编译器依赖，同步 Gradle 后编译正常。

## 九、实验总结
本次实验完整完成了 Room 数据库的集成与使用，从零实现了 Entity 实体映射、DAO 数据查询、Room 数据库初始化，完成了 ViewModel 数据层的改造，成功替换静态模拟数据为本地预置数据库真实数据。同时深入理解了 Flow 响应式数据流、ViewModel 生命周期管理、Compose 状态订阅的完整数据流转链路。

通过本次实验，熟练掌握了 Android Room 本地持久化框架的核心用法，理解了分层架构中数据层、业务层、UI 层的职责分离，为后续 Android 本地数据库开发、响应式 UI 开发奠定了基础。