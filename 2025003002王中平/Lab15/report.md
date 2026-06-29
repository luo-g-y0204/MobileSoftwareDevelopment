# Lab15 Flight Search 航班搜索应用实验报告
## 一、实验概述
### 1. 实验目的
1. 掌握Room持久化框架，完成预置SQLite数据库读取、单表查询、多表联合查询、增删改查操作；
2. 学会使用Preferences DataStore实现轻量数据持久化，保存用户搜索记录；
3. 利用SQL `LIKE` 模糊查询实现输入自动补全；
4. 使用Jetpack Compose搭建多层切换UI，结合Flow/StateFlow管理异步数据流；
5. 通过ViewModel统一管理页面UI状态，实现屏幕旋转、应用重启状态保存。

### 2. 实验环境
- 开发工具：Android Studio Hedgehog
- 开发语言：Kotlin
- 核心框架：Jetpack Compose、Room 2.6.1、Preferences DataStore、KSP
- 构建工具：Gradle 8.6、AGP 8.4.0
- 数据库：预置flight_search.db

### 3. 应用核心功能
1. 机场模糊搜索自动补全；
2. 选中出发机场展示全部目的地航班；
3. 航班航线收藏/取消收藏；
4. 搜索框清空时展示全部收藏航线；
5. 应用重启自动恢复上次搜索文本。

## 二、数据层设计说明
### 1. Entity 实体类设计（数据表映射）
#### （1）Airport.kt —— 映射airport机场表
```kotlin
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey val id: Int,
    val iata_code: String,
    val name: String,
    val passengers: Int
)
```
- 映射数据库`airport`表，主键id自增唯一；
- `iata_code`存储3位机场代码，`name`存储机场全称，`passengers`年客流量用于自动补全排序；
- 用于搜索补全、展示出发/目的地机场信息。

#### （2）Favorite.kt —— 映射favorite收藏表
```kotlin
@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val departure_code: String,
    val destination_code: String
)

// 联合查询封装类，存储完整航线信息
data class FavoriteRoute(
    val favoriteId: Int,
    val departureIata: String,
    val departureName: String,
    val destIata: String,
    val destName: String
)
```
1. `Favorite`实体：映射收藏表，仅存储出发、目的地IATA代码，主键自动生成；
2. `FavoriteRoute`数据类：无数据库映射，用于多表JOIN查询结果封装，一次性携带出发、目的地完整机场名称，避免多次数据库IO。

### 2. FlightDao.kt 数据访问层查询设计
DAO层封装全部数据库操作，所有查询返回Flow实现数据实时监听：
1. **自动补全搜索方法：searchAirports(query: String)**
    ```sql
    SELECT * FROM airport WHERE iata_code LIKE :query OR name LIKE :query ORDER BY passengers DESC
    ```
    - 功能：模糊匹配机场代码/名称；
    - 排序规则：按年客流量降序，热门机场优先展示；
    - 参数拼接`%query%`实现全局模糊匹配。

2. **目的地航班查询：getDestinationsFromAirport(departIata: String)**
    ```sql
    SELECT * FROM airport WHERE iata_code != :departIata
    ```
    - 功能：查询除自身外所有机场作为目的地，模拟全航线航班；
    - 过滤条件：排除出发机场自身，避免无效航线。

3. **收藏联合查询：getAllFavoriteRoutes()**
    ```sql
    SELECT f.id as favoriteId, dep.iata_code as departureIata, dep.name as departureName, dest.iata_code as destIata, dest.name as destName
    FROM favorite f
    INNER JOIN airport dep ON f.departure_code = dep.iata_code
    INNER JOIN airport dest ON f.destination_code = dest.iata_code
    ```
    - 双INNER JOIN关联airport表，将收藏表存储的简短IATA代码转换为完整机场名称；
    - 一次性返回页面渲染所需全部字段，优化查询性能。

4. **收藏单条查询：getFavoriteByRoute(dep: String, dest: String)**
    - 根据出发、目的地代码匹配收藏记录，用于判断当前航线是否已收藏，控制UI收藏按钮图标。

5. **收藏CRUD方法**
    - `insertFavorite()`：新增收藏航线；
    - `deleteFavorite()`：删除指定收藏记录。

### 3. Room数据库类 FlightDatabase.kt
1. 标注`@Database`声明两张实体表，数据库版本1；
2. 单例模式保证全局仅一个数据库实例；
3. 使用`.createFromAsset("database/flight_search.db")`加载assets目录预置数据库，无需代码初始化填充数据；
4. 对外暴露FlightDao对象提供数据库访问入口。

## 三、SQL关键语法说明
### 1. LIKE 模糊匹配关键字
1. 使用方式：查询参数前后拼接通配符`%`，即`"%$input%"`；
2. 作用：`%`代表任意长度任意字符，实现**全局包含匹配**；
    - 输入`sh`可匹配SHA、SHANGHAI等所有包含sh的机场；
3. 优势：满足实时输入自动补全需求，仅返回匹配结果，不会一次性加载全表数据，降低内存占用。

### 2. INNER JOIN 联合查询
1. 使用场景：`favorite`表仅存储IATA代码，页面展示需要机场完整名称；
2. 实现逻辑：将favorite表分别与两张airport表建立关联，分别读取出发、目的地机场信息；
3. 性能优势：单次SQL完成多表数据读取，替代多次单表循环查询，减少数据库IO次数。

## 四、Preferences DataStore 持久化模块
### 1. 实现文件 UserPreferencesRepository.kt
1. 定义全局DataStore实例，存储key为`search_text`的字符串偏好；
2. 提供两个核心能力：
    - `savedSearchText: Flow<String>`：流式读取保存的搜索文本，无数据时返回空字符串；
    - `suspend fun saveSearchText(text: String)`：协程内异步写入用户输入文本；
3. 使用场景：
    - 用户输入内容实时保存；
    - 应用重启时自动读取历史文本，回填搜索框；
    - 若上次退出时输入框为空，启动后直接展示收藏列表。
4. 优势：替代SharedPreferences，基于协程Flow异步读写，线程安全、兼容Compose数据流。

## 五、ViewModel 状态管理设计 FlightViewModel.kt
### 1. 核心职责
作为UI与数据层中间层，统一管理页面所有状态，隔离数据库、DataStore操作，避免Compose直接操作数据源。

### 2. 状态拆分
1. 基础输入状态：`_searchInput` MutableStateFlow存储用户输入文本；
2. 自动补全数据流：`airportSuggestions`，输入变化自动触发数据库模糊查询；
3. 选中出发机场：`_selectedDepart`，记录用户点击的机场；
4. 目的地航班数据流：`destAirports`，选中机场后自动查询全部目的地；
5. 收藏航线数据流：`favoriteRoutes`，监听收藏表实时更新；
6. 整合总状态`FlightUiState`：使用`combine`合并所有数据流，Compose页面仅需收集单一UiState即可获取全部页面数据。

### 3. 核心业务方法
1. `updateSearchInput(text: String)`：更新输入框内容，同时调用DataStore持久化；输入为空时清空选中机场；
2. `selectDepartAirport(airport: Airport)`：记录用户选中的出发机场，触发目的地查询；
3. `addFavorite/removeFavorite`：协程内调用Dao完成收藏增删；
4. `isRouteFav(dep, dest)`：返回单条航线收藏状态流，控制按钮显示。

### 4. 生命周期优化
所有Flow使用`stateIn(SharingStarted.WhileSubscribed(5000))`缓存数据，页面不可见时自动取消数据库监听，避免资源浪费。

## 六、Jetpack Compose UI界面逻辑 FlightScreen.kt
### 1. 页面切换逻辑（三层视图互斥展示）
通过判断UiState中`searchInput`、`selectedDepartAirport`两个状态自动切换页面：
1. **输入框有文字**：展示LazyColumn自动补全机场建议列表；点击条目触发选择机场；
2. **存在选中出发机场**：隐藏补全列表，展示全部目的地航班条目；每条航班携带收藏按钮；
3. **输入框为空、无选中机场**：展示全部收藏航线列表，提供删除收藏按钮。

### 2. 页面组件拆分
1. 搜索输入框：OutlinedTextField，双向绑定ViewModel搜索文本；
2. AirportSuggestItem：自动补全机场条目卡片，点击选中机场；
3. FlightDestItem：航班目的地条目，单独监听当前航线收藏状态，切换实心/空心爱心图标；
4. FavoriteRouteItem：收藏航线条目，展示完整航线并支持删除收藏；
5. 全部列表使用LazyColumn实现高性能滚动，仅渲染屏幕可见条目。

### 3. 数据流收集
使用`collectAsStateWithLifecycle()`收集ViewModel中的Flow，遵循Android生命周期，页面销毁自动取消监听，防止内存泄漏。

## 七、实验过程遇到的问题与解决方案
### 问题1：Version Catalog libs.versions.toml 重复from()报错
- 现象：Gradle提示同一个catalog只能调用一次from方法，同步失败；
- 原因：settings.gradle.kts中重复配置versionCatalogs，新版Gradle限制多文件导入；
- 解决方案：彻底删除libs.versions.toml文件，移除settings中versionCatalogs全部代码，所有依赖直接硬编码版本号，规避版本目录冲突。

### 问题2：Gradle同步报错 Unable to load class 'org.gradle.api.internal.HasConvention'
- 现象：Gradle内部类找不到，构建流程中断；
- 原因：AGP插件版本与Gradle发行版版本不匹配，高AGP要求更高版本Gradle；
- 解决方案：统一版本组合 AGP 8.4.0 + Gradle 8.6；清理项目.gradle、全局Gradle缓存，杀死全部Java进程后重启同步。

### 问题3：Gradle官方分发包下载超时 SocketTimeoutException
- 现象：国外官方服务器网络延迟高，下载gradle-all.zip中断；
- 解决方案：修改gradle-wrapper.properties的distributionUrl为腾讯云国内镜像地址，加速下载；同时settings配置阿里云Maven镜像，优化依赖同步速度。

### 问题4：预置flight_search.db数据库读取失败
- 现象：运行应用数据库为空，无法查询机场数据；
- 原因：assets文件夹层级错误，文件未放置在`src/main/assets/database/`下；
- 解决方案：新建完整目录层级，数据库文件路径与Room createFromAsset参数保持一致，区分大小写。

### 问题5：收藏按钮状态不同步，点击后图标不实时更新
- 现象：新增/删除收藏后，页面爱心图标无变化；
- 原因：未单独监听单条航线的收藏Flow，仅一次性加载收藏列表；
- 解决方案：每条航班条目独立调用`isRouteFav`获取单独Flow，实时监听当前航线收藏状态。

### 问题6：应用重启后搜索文本丢失
- 现象：关闭重开App，输入框无历史记录；
- 原因：仅在输入时保存文本，但ViewModel初始化未读取DataStore数据回填；
- 解决方案：ViewModel init代码块中收集savedSearchText流，初始化时自动赋值给搜索输入状态。

## 八、实验总结
本次实验完整实现了基于Room+DataStore+Compose的航班搜索应用，覆盖关系数据库模糊查询、多表联合查询、本地轻量持久化、MVVM状态管理、Compose多视图切换等核心知识点。
1. 通过Room掌握了预置外部数据库加载、流式实时数据监听、复杂SQL语句编写；
2. 理解`LIKE`模糊匹配、INNER JOIN多表关联查询的实际业务场景用法；
3. 学会使用DataStore替代传统SharedPreferences，实现安全的异步数据持久化；
4. 借助ViewModel+Flow统一管理UI状态，实现页面数据与业务逻辑解耦；
5. 掌握Compose动态切换页面、列表高性能渲染、生命周期安全数据流收集；
6. 同时解决了国内环境下Gradle版本兼容、网络下载超时等工程配置问题，熟悉Android构建体系排错思路。