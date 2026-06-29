1. Lab15 Flight Search 航班搜索应用实验报告

   ## 一、实验概述

   ### 1. 实验目的

   - 掌握 Room 持久化框架，完成预置 SQLite 数据库的读取、单表查询、多表联合查询及增删改查操作；
   - 学会使用 Preferences DataStore 实现轻量数据持久化，保存用户搜索记录；
   - 利用 SQL `LIKE` 模糊查询实现输入自动补全；
   - 使用 Jetpack Compose 搭建多层切换 UI，结合 Flow/StateFlow 管理异步数据流；
   - 通过 ViewModel 统一管理页面 UI 状态，实现屏幕旋转、应用重启后的状态保留。

   ### 2. 实验环境

   - 开发工具：Android Studio Hedgehog
   - 开发语言：Kotlin
   - 核心框架：Jetpack Compose、Room 2.6.1、Preferences DataStore、KSP
   - 构建工具：Gradle 8.6、AGP 8.4.0
   - 数据库：预置 `flight_search.db`

   ### 3. 应用核心功能

   - 机场模糊搜索自动补全；
   - 选中出发机场后展示全部目的地航班；
   - 航班航线收藏 / 取消收藏；
   - 搜索框清空时展示全部收藏航线；
   - 应用重启自动恢复上次搜索文本。

   ------

   ## 二、数据层设计说明

   ### 1. Entity 实体类设计（数据表映射）

   #### （1）Airport.kt —— 映射 airport 机场表

   kotlin

   ```
   @Entity(tableName = "airport")
   data class Airport(
       @PrimaryKey val id: Int,
       val iata_code: String,
       val name: String,
       val passengers: Int
   )
   ```

   

   - 映射 `airport` 表，主键 `id` 自增唯一；
   - `iata_code` 存储 3 位机场代码，`name` 存储机场全称，`passengers`（年客流量）用于补全排序；
   - 用于搜索补全及展示出发/目的地机场信息。

   #### （2）Favorite.kt —— 映射 favorite 收藏表

   kotlin

   ```
   @Entity(tableName = "favorite")
   data class Favorite(
       @PrimaryKey(autoGenerate = true) val id: Int = 0,
       val departure_code: String,
       val destination_code: String
   )
   
   // 联合查询结果封装类（非数据库实体）
   data class FavoriteRoute(
       val favoriteId: Int,
       val departureIata: String,
       val departureName: String,
       val destIata: String,
       val destName: String
   )
   ```

   

   - `Favorite` 实体：仅存储出发、目的地 IATA 代码，主键自动生成；
   - `FavoriteRoute` 数据类：无数据库映射，用于多表 JOIN 查询结果封装，一次性携带完整机场名称，避免多次 IO。

   ### 2. FlightDao.kt —— 数据访问层

   所有查询均返回 `Flow`，实现数据实时监听。

   - **自动补全搜索**：`searchAirports(query: String)`

     sql

     ```
     SELECT * FROM airport 
     WHERE iata_code LIKE :query OR name LIKE :query 
     ORDER BY passengers DESC
     ```

     

     - 对代码或名称进行全局模糊匹配（`%query%`）；
     - 按客流量降序，热门机场优先展示。

   - **目的地航班查询**：`getDestinationsFromAirport(departIata: String)`

     sql

     ```
     SELECT * FROM airport WHERE iata_code != :departIata
     ```

     

     - 排除出发机场自身，返回所有其他机场作为目的地。

   - **收藏联合查询**：`getAllFavoriteRoutes()`

     sql

     ```
     SELECT f.id as favoriteId, 
            dep.iata_code as departureIata, 
            dep.name as departureName, 
            dest.iata_code as destIata, 
            dest.name as destName
     FROM favorite f
     INNER JOIN airport dep ON f.departure_code = dep.iata_code
     INNER JOIN airport dest ON f.destination_code = dest.iata_code
     ```

     

     - 双 INNER JOIN 将 IATA 代码转换为完整名称，一次查询返回渲染所需全部字段。

   - **收藏单条查询**：`getFavoriteByRoute(dep: String, dest: String)` —— 用于判断当前航线是否已收藏，控制 UI 收藏按钮状态。

   - **收藏 CRUD**：`insertFavorite()` / `deleteFavorite()`

   ### 3. Room 数据库类 FlightDatabase.kt

   - 标注 `@Database`，声明两张实体表，版本号为 1；
   - 单例模式保证全局唯一实例；
   - 使用 `.createFromAsset("database/flight_search.db")` 加载 assets 中的预置数据库，无需代码初始化；
   - 对外暴露 `FlightDao` 提供访问入口。

   ------

   ## 三、SQL 关键语法说明

   ### 1. LIKE 模糊匹配

   - 使用方式：查询参数前后拼接 `%`，即 `"%$input%"`，实现**全局包含匹配**（例如输入 `sh` 可匹配 `SHA`、`SHANGHAI` 等）；
   - 优势：满足实时输入补全需求，仅返回匹配结果，避免全表加载，降低内存占用。

   ### 2. INNER JOIN 联合查询

   - 使用场景：`favorite` 表仅存 IATA 代码，页面需展示机场完整名称；
   - 实现：将 `favorite` 分别与两个 `airport` 表关联，分别读取出发、目的地信息；
   - 性能优势：单次 SQL 完成多表关联，减少数据库 IO 次数。

   ------

   ## 四、Preferences DataStore 持久化模块

   - **实现文件**：`UserPreferencesRepository.kt`
   - 定义全局 DataStore 实例，存储键名为 `search_text` 的字符串偏好；
   - 提供两个核心能力：
     - `savedSearchText: Flow<String>`：流式读取保存的文本，无数据时返回空串；
     - `suspend fun saveSearchText(text: String)`：协程内异步写入用户输入。
   - **使用场景**：
     - 用户输入实时保存；
     - 应用重启时自动读取历史文本回填搜索框；
     - 若上次退出时输入为空，启动后直接展示收藏列表。
   - **优势**：基于协程 Flow 异步读写，线程安全，天然兼容 Compose 数据流。

   ------

   ## 五、ViewModel 状态管理设计（FlightViewModel.kt）

   ### 1. 核心职责

   作为 UI 与数据层的中间层，统一管理页面状态，隔离数据库和 DataStore 操作，避免 Composable 直接接触数据源。

   ### 2. 状态拆分

   - `_searchInput`：MutableStateFlow 存储当前输入文本；
   - `airportSuggestions`：输入变化自动触发数据库模糊查询，返回补全列表；
   - `_selectedDepart`：记录用户选中的出发机场；
   - `destAirports`：选中出发机场后自动查询所有目的地；
   - `favoriteRoutes`：监听收藏表实时更新；
   - **整合状态 `FlightUiState`**：使用 `combine` 合并以上所有数据流，页面只需收集单一 UiState 即可获取全部数据。

   ### 3. 核心业务方法

   - `updateSearchInput(text: String)`：更新输入并持久化至 DataStore；输入为空时清空选中机场；
   - `selectDepartAirport(airport: Airport)`：记录出发机场，触发目的地查询；
   - `addFavorite` / `removeFavorite`：协程内调用 DAO 完成收藏增删；
   - `isRouteFav(dep, dest)`：返回单条航线的收藏状态流，用于控制收藏按钮图标。

   ### 4. 生命周期优化

   所有 Flow 使用 `stateIn(SharingStarted.WhileSubscribed(5000))` 缓存数据，页面不可见时自动取消数据库监听，避免资源浪费。

   ------

   ## 六、Jetpack Compose UI 界面逻辑（FlightScreen.kt）

   ### 1. 三层视图互斥切换

   根据 `UiState` 中的 `searchInput` 和 `selectedDepartAirport` 自动切换：

   - **输入框有文字** → 展示 `LazyColumn` 自动补全建议列表；
   - **存在选中出发机场** → 隐藏补全列表，展示全部目的地航班条目（每条附带收藏按钮）；
   - **输入框为空且无选中机场** → 展示全部收藏航线列表（每条附带删除按钮）。

   ### 2. 组件拆分

   - **搜索输入框**：`OutlinedTextField`，双向绑定 ViewModel 的搜索文本；
   - **`AirportSuggestItem`**：补全条目卡片，点击即选中机场；
   - **`FlightDestItem`**：航班目的地条目，独立监听当前航线收藏状态，切换实心/空心爱心图标；
   - **`FavoriteRouteItem`**：收藏航线条目，展示完整航线并支持删除；
   - 所有列表均使用 `LazyColumn` 实现高性能滚动，仅渲染可见条目。

   ### 3. 数据流收集

   使用 `collectAsStateWithLifecycle()` 收集 ViewModel 中的 Flow，遵循 Android 生命周期，页面销毁时自动取消监听，防止内存泄漏。

   ------

   ## 七、实验过程遇到的问题与解决方案

   | 问题                         | 现象                                                         | 解决方案                                                     |
   | :--------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
   | **Version Catalog 重复配置** | Gradle 提示 `from()` 只能调用一次                            | 删除 `libs.versions.toml`，移除 `settings.gradle.kts` 中的 versionCatalogs 配置，所有依赖硬编码版本号。 |
   | **Gradle 内部类找不到**      | `Unable to load class 'org.gradle.api.internal.HasConvention'` | 统一 AGP 8.4.0 + Gradle 8.6；清理项目 `.gradle` 和全局缓存，重启同步。 |
   | **Gradle 分发包下载超时**    | `SocketTimeoutException`                                     | 修改 `gradle-wrapper.properties` 中的 `distributionUrl` 为腾讯云国内镜像，并在 `settings` 中配置阿里云 Maven 镜像。 |
   | **预置数据库读取失败**       | 应用运行时数据库为空                                         | 确保数据库文件置于 `src/main/assets/database/` 目录，路径与 `createFromAsset` 参数严格一致（区分大小写）。 |
   | **收藏按钮状态不同步**       | 点击收藏/取消后图标不实时更新                                | 每条航班条目独立调用 `isRouteFav` 获取单独的收藏状态 Flow，实时监听变化。 |
   | **应用重启后搜索文本丢失**   | 关闭重开，输入框无历史记录                                   | 在 ViewModel 的 `init` 块中收集 `savedSearchText` 流，初始化时自动赋值给搜索输入状态。 |

   ------

   ## 八、实验总结

   本次实验完整实现了基于 Room + DataStore + Compose 的航班搜索应用，覆盖了关系数据库模糊查询、多表联合查询、本地轻量持久化、MVVM 状态管理及 Compose 多视图切换等核心知识点。

   - 通过 Room 掌握了预置外部数据库加载、流式实时监听及复杂 SQL 编写；
   - 理解了 `LIKE` 模糊匹配和 `INNER JOIN` 多表关联的实际业务用法；
   - 学会了使用 DataStore 替代 SharedPreferences，实现安全的异步数据持久化；
   - 借助 ViewModel + Flow 实现了 UI 状态统一管理，达成业务逻辑与界面解耦；
   - 掌握了 Compose 动态页面切换、高性能列表渲染及生命周期安全的数据收集；
   - 同时解决了国内环境下 Gradle 版本兼容、依赖下载超时等工程配置问题，积累了 Android 构建体系的排错经验。