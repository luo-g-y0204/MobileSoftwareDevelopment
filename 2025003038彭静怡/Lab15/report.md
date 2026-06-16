# Lab15 航班搜索应用实验报告
## 一、实验概述
本实验基于 **Android Jetpack 技术栈** 实现航班搜索应用，核心技术包含 **Room 本地数据库**、**DataStore 偏好存储**、**Kotlin Flow 响应式数据流**、**ViewModel 状态管理** 以及 **Jetpack Compose UI**。应用实现机场搜索、航班路线展示、航线收藏、搜索文本持久化等功能，完整覆盖本地数据库设计、复杂SQL查询、数据持久化、状态流转与界面联动等知识点。

## 二、核心功能与验收情况
1. 项目可正常编译、运行，已引入 Room、DataStore、Compose 全套依赖；
2. 完成 `Airport`、`Favorite` 两张数据表的 Entity 映射与建表；
3. DAO 实现自动补全、航班查询、收藏增删改查、联表查询等全部接口；
4. 基于 `LIKE` 关键字实现机场模糊搜索，基于 `CROSS JOIN`/`INNER JOIN` 实现多表联合查询；
5. 使用 Preferences DataStore 持久化搜索文本，应用重启后数据不丢失；
6. ViewModel 结合 Flow 完成全局状态管理、数据流组合与业务逻辑处理；
7. Compose 基于状态分支实现多界面动态切换，UI 交互符合需求；
8. 完整实现**机场自动补全、航班展示、航线收藏/取消、收藏列表展示**四大核心业务。

---

## 三、详细模块设计说明
### 1. Entity 实体类设计（数据表映射）
Room 通过 `@Entity` 注解将 Kotlin 数据类映射为 SQLite 数据表，本项目共定义 **airport（机场表）**、**favorite（收藏航线表）** 两张核心数据表，结合主键、字段别名完成映射。

#### 1.1 Airport 实体（airport 表）
文件：`Airport.kt`
- 作用：存储所有机场基础信息，包含编号、名称、IATA 三字码、年客流量；
- 注解说明：
  1. `@Entity(tableName = "airport")`：声明该类对应数据库中 `airport` 表；
  2. `@PrimaryKey(autoGenerate = true)`：自增整型主键 `id`，作为表唯一标识；
  3. `@ColumnInfo(name = "iata_code")`：字段别名映射，将 Kotlin 属性 `iataCode` 对应数据库列 `iata_code`（适配SQL命名规范）。
- 字段说明：

| Kotlin 属性 | 数据库列名 | 类型 | 作用 |
| ---- | ---- | ---- | ---- |
| id | id | Int | 自增主键 |
| name | name | String | 机场全称 |
| iataCode | iata_code | String | 机场IATA三字代码（搜索核心字段） |
| passengers | passengers | Int | 机场年客流量（排序依据） |

#### 1.2 Favorite 实体（favorite 表）
文件：`Favorite.kt`
- 作用：仅存储收藏航线的**出发/到达IATA代码**，精简存储，通过联表查询补充机场名称；
- 设计思路：避免重复存储机场名称，降低数据冗余，仅保存关联键。
- 字段说明：

| Kotlin 属性 | 数据库列名 | 类型 | 作用 |
| ---- | ---- | ---- | ---- |
| id | id | Int | 自增主键 |
| departureCode | departure_code | String | 出发机场IATA代码 |
| destinationCode | destination_code | String | 目的机场IATA代码 |

#### 1.3 数据库总配置
文件：`FlightDatabase.kt`
- 通过 `@Database` 注解绑定两张实体类，数据库版本为 1；
- 内置单例模式，通过 `createFromAsset` 加载预置机场数据；
- 对外暴露 `FlightDao` 数据访问接口，统一管理数据库实例。

---

### 2. DAO 数据访问层设计（FlightDao）
文件：`FlightDao.kt`
DAO 是 Room 数据访问核心，通过 `@Dao` 注解声明接口，结合 `@Query`、`@Insert`、`@Delete` 实现数据库操作。本项目所有查询均结合 **Kotlin Flow** 实现**响应式监听**，数据变更自动推送至上层，适配 Compose 实时刷新。

#### 2.1 基础查询接口
1. `observeAllAirports()`：查询全部机场，按客流量 **降序** 排列，返回 `Flow` 数据流；
2. `getAirportByCode(iataCode)`：根据IATA代码精准查询单个机场，挂起函数适配协程。

#### 2.2 自动补全查询（核心模糊搜索）
方法：`searchAirports(query: String)`
- 功能：输入关键词，**同时匹配机场名称和IATA代码**，实现搜索自动补全；
- SQL 核心逻辑：
  - 使用 `LIKE` 关键字实现模糊匹配，`%` 为通配符；
  - `COLLATE NOCASE`：忽略大小写，兼容大小写输入；
  - `LIMIT 10`：限制返回条数，避免一次性加载全表数据，优化性能；
  - 结果按客流量降序排序，优先展示大型机场。

#### 2.3 航班路线查询（CROSS JOIN 交叉连接）
方法：`observeFlightsFrom(departureCode: String)`
- 业务逻辑：假设当前机场可飞往所有其他机场，生成全量航线；
- SQL 核心：使用 `CROSS JOIN` 交叉连接 `airport` 表自身，实现**机场 × 机场**全组合；
- 过滤条件：排除出发机场自身（`destination.iata_code != :departureCode`）；
- 排序规则：按目的机场客流量降序展示航线。

#### 2.4 收藏相关查询（联表查询 + 增删改查）
1. `observeFavoriteRoutes()`：**多表内连接查询**，将 `favorite` 收藏表与 `airport` 机场表关联，补全机场名称，用于收藏列表展示；
2. `observeFavorites()`：仅查询收藏表原始数据（IATA代码），用于判断航线收藏状态；
3. `isFavorite()`：通过 `EXISTS` 子查询，判断指定航线是否已收藏；
4. `insertFavorite()`：插入收藏记录，`OnConflictStrategy.IGNORE` 避免重复收藏；
5. `deleteFavorite()`：根据出发/到达代码删除指定收藏航线。

---

### 3. LIKE 关键字使用方法与作用
#### 3.1 基本概念
`LIKE` 是 SQLite 标准关键字，用于**模糊匹配字符串**，是本项目**机场自动补全**功能的核心。搭配通配符 `%` 使用，`%` 代表**任意长度的任意字符**（包含空字符）。

#### 3.2 本项目中使用方式
```sql
WHERE iata_code LIKE '%' || :query || '%' COLLATE NOCASE
   OR name LIKE '%' || :query || '%' COLLATE NOCASE
```
1. **拼接规则**：`'%' || :query || '%'`
   - 前后均加 `%`：**全模糊匹配**，关键词可出现在字段任意位置；
   - 示例：输入 `LA`，可匹配 LAX、LGA、Los Angeles 等所有包含 `LA` 的机场。
2. **`COLLATE NOCASE`**：大小写不敏感，输入 `lax` 和 `LAX` 效果一致，提升用户体验。
3. **双字段匹配**：同时对 `iata_code`（IATA代码）和 `name`（机场名称）做模糊查询，兼顾代码搜索与中文/英文名称搜索。

#### 3.3 作用总结
1. 实现**搜索自动补全**，无需用户输入完整名称/代码即可匹配结果；
2. 提升搜索灵活性，兼容部分输入、大小写混乱等场景；
3. 配合 `LIMIT` 限制结果集，平衡功能与性能。

---

### 4. 联合查询（JOIN）的实现和作用
本项目使用两种联合查询：**交叉连接（CROSS JOIN）**、**内连接（INNER JOIN）**，分别对应航班生成、收藏列表两大场景。

#### 4.1 CROSS JOIN 交叉连接（航班路线生成）
1. 实现代码：
```sql
FROM airport AS departure
CROSS JOIN airport AS destination
```
2. 原理：将 `airport` 表**自连接**，生成 `机场数量 × 机场数量` 的全组合结果，代表所有可能的出发→到达航线；
3. 过滤逻辑：通过 `WHERE` 排除“出发=到达”的无效航线；
4. 作用：快速生成当前机场的所有可达航线，无需手动录入航线数据，数据结构极简。

#### 4.2 INNER JOIN 内连接（收藏列表查询）
1. 实现代码：
```sql
FROM favorite
INNER JOIN airport AS departure ON favorite.departure_code = departure.iata_code
INNER JOIN airport AS destination ON favorite.destination_code = destination.iata_code
```
2. 原理：以 `favorite` 收藏表为基准，通过**IATA代码**关联两张 `airport` 表，将收藏表的代码替换为完整机场名称；
3. 核心优势：
   - 数据解耦：收藏表仅存储关联键，不冗余存储机场名称；
   - 数据一致性：机场信息更新后，收藏列表自动同步，无需修改收藏表；
4. 作用：将精简的收藏原始数据，拼接为可直接展示的完整航线信息（代码+名称）。

#### 4.3 联合查询整体价值
- 实现**多表数据联动**，解决单表数据不足的问题；
- 遵循数据库设计范式，减少数据冗余；
- 依托数据库原生查询能力，比代码层拼接数据效率更高。

---

### 5. Preferences DataStore 使用场景与实现
文件：`UserPreferencesRepository.kt`
DataStore 是 Google 推荐替代 SharedPreferences 的轻量偏好存储组件，基于数据流实现异步、一致性数据读写，本项目用于**持久化搜索文本**。

#### 5.1 使用场景
1. 核心场景：保存用户最后一次输入的**搜索关键词**；
2. 业务需求：应用重启后，自动恢复上一次的搜索内容；
3. 扩展场景：若搜索文本为完整3位IATA代码，重启后直接加载对应机场的航班列表。

#### 5.2 完整实现步骤
1. **定义存储键**：使用 `stringPreferencesKey` 定义字符串类型键 `SEARCH_TEXT`，用于存储搜索文本；
2. **初始化 DataStore**：通过 `by preferencesDataStore` 绑定文件名称 `flight_search_preferences`，全局唯一实例；
3. **数据流读取**：
   - 对外暴露 `searchTextFlow: Flow<String>`；
   - 捕获 IO 异常，防止文件读写崩溃；
   - 从 DataStore 中读取键对应的值，无数据时返回空字符串；
4. **数据写入**：`saveSearchText(text: String)` 挂起函数，通过 `dataStore.edit` 修改偏好数据；
5. **上层调用**：ViewModel 在初始化时读取历史搜索文本，搜索、清空、选择机场时实时写入最新文本。

#### 5.3 优势对比（相较于 SharedPreferences）
1. 基于 Flow 响应式数据流，天然适配 ViewModel 与 Compose；
2. 完全异步操作，无主线程阻塞风险；
3. 支持异常捕获，稳定性更强。

---

### 6. ViewModel 状态管理设计
文件：`FlightViewModel.kt`
ViewModel 作为**数据层与UI层的中间枢纽**，结合 **Flow、组合流、状态流** 实现全局状态统一管理，拆分业务逻辑，保证 UI 与数据解耦。

#### 6.1 核心数据模型
1. `FlightUiState`：UI 全局状态类，包含搜索文本、选中机场、搜索建议、航班列表、收藏列表、加载状态，Compose 界面唯一依赖数据源；
2. `FlightItem`：UI 层航班模型，在原始航线基础上增加 `isFavorite` 收藏状态；
3. `FlightContentState`：中间状态类，聚合搜索建议、航班、收藏三大数据流，简化组合逻辑。

#### 6.2 数据流分层设计
1. **基础状态流（MutableStateFlow）**
   - `searchText`：监听搜索框文本变化；
   - `selectedAirport`：监听当前选中的出发机场；
   - `initialized`：标记数据初始化完成，控制加载状态。

2. **业务数据流（Flow 转换）**
   - 搜索建议流：对 `searchText` 做 **debounce(250) 防抖** + `distinctUntilChanged` 去重，避免频繁触发搜索；根据文本和选中机场动态切换“显示建议/隐藏建议”；
   - 收藏键流：将收藏列表转换为 `(出发代码, 到达代码)` 键集合，用于批量判断航班收藏状态；
   - 航班流：根据选中机场查询航线，结合收藏键流批量绑定收藏状态，生成 `FlightItem`；
   - 内容状态流：组合搜索建议、航班、收藏三大数据流。

3. **全局UI状态流（uiState）**
   - 通过 `combine` 组合所有基础流与业务流，统一封装为 `FlightUiState`；
   - 调用 `stateIn` 转换为**状态流**，绑定 `viewModelScope`，生命周期跟随 ViewModel，UI 订阅后自动刷新。

#### 6.3 核心业务方法
1. `updateSearchText()`：更新搜索文本、清空选中机场、持久化文本到 DataStore；
2. `selectAirport()`：选中机场后，回填IATA代码到搜索框，切换为航班列表；
3. `clearSearch()`：清空搜索内容、选中机场，恢复默认收藏页面；
4. `toggleFavorite()`：切换航线收藏状态，调用仓库层完成数据库增删；
5. `restoreSearchText()`：初始化时读取 DataStore 历史文本，恢复搜索状态。

#### 6.4 设计亮点
1. **防抖优化**：搜索文本添加 250ms 防抖，减少数据库查询次数；
2. **状态聚合**：单一 `uiState` 驱动整个UI，状态单一来源，避免多状态混乱；
3. **解耦分层**：ViewModel 不直接操作数据库，依赖 Repository 仓库层，符合架构规范；
4. **生命周期安全**：Flow 结合 `SharingStarted.WhileSubscribed`，UI 不可见时自动停止数据流，节约资源。

---

### 7. UI 界面切换逻辑说明
文件：`FlightScreen.kt`
基于 Jetpack Compose 实现界面，使用 `Crossfade` 动画组件完成多页面平滑切换，**完全由 ViewModel 下发的 `uiState` 驱动界面分支**。

#### 7.1 界面模式枚举
定义 `ScreenMode` 区分四大界面状态：
- `Loading`：加载中（应用初始化阶段）；
- `Favorites`：收藏航线列表（搜索框为空时默认页面）；
- `Suggestions`：机场搜索建议列表（搜索框有文本、未选中机场）；
- `Flights`：航班路线列表（已选中出发机场）。

#### 7.2 界面切换判断逻辑
```kotlin
val screenMode = when {
    uiState.isLoading -> ScreenMode.Loading          // 初始化加载
    uiState.searchText.isBlank() -> ScreenMode.Favorites // 无搜索文本，展示收藏
    uiState.selectedAirport == null -> ScreenMode.Suggestions // 有文本、未选机场，展示搜索建议
    else -> ScreenMode.Flights // 已选机场，展示航班列表
}
```
切换规则优先级：**加载状态 > 空搜索文本 > 有文本未选机场 > 已选机场**。

#### 7.3 各界面功能说明
1. **搜索栏（SearchField）**：全局顶部搜索框，支持输入、清空、搜索图标引导，键盘自动大写适配IATA代码；
2. **加载页（LoadingContent）**：展示环形进度条，应用初始化时显示；
3. **收藏页（FavoritesContent）**：无搜索内容时默认展示，支持删除收藏航线，无数据时展示空状态提示；
4. **搜索建议页（SuggestionsContent）**：模糊搜索结果列表，点击机场项即可选中并跳转航班页；
5. **航班页（FlightsContent）**：展示当前机场所有航线，右侧爱心图标切换收藏/取消收藏；
6. **通用组件**：`IataBadge`（IATA代码标签）、`RouteAirportLine`（航线行布局）、空状态提示组件，复用降低冗余。

#### 7.4 交互联动逻辑
1. 输入关键词 → 触发模糊搜索 → 展示搜索建议；
2. 点击搜索建议中的机场 → 自动回填IATA代码 → 切换至航班列表；
3. 航班列表点击爱心 → 切换收藏状态 → 数据库同步更新；
4. 清空搜索框 → 回到收藏列表；
5. 应用重启 → DataStore 恢复搜索文本 → 自动还原上一次界面状态。

---

### 8. 实验中遇到的问题与解决过程
#### 问题1：模糊搜索大小写不匹配，输入小写代码无法匹配数据
- 现象：数据库中IATA代码为大写，用户输入小写字母时，`LIKE` 查询无结果；
- 解决方案：在 SQL 语句中添加 `COLLATE NOCASE` 修饰符，强制查询忽略大小写，兼容大小写输入。

#### 问题2：输入单个字母时，加载全量机场数据，界面卡顿
- 现象：搜索框输入单个字符，SQL 查询返回所有机场，列表渲染卡顿；
- 解决方案：在搜索SQL中增加 `LIMIT 10`，限制最大返回条数，优化查询与渲染性能。

#### 问题3：收藏状态不同步，航班列表爱心图标状态滞后
- 现象：添加/取消收藏后，UI 图标没有立即刷新，数据流延迟；
- 原因：航班列表与收藏列表为两条独立 Flow，状态未联动；
- 解决方案：将收藏列表转换为 **代码键集合**，通过 `combine` 与航班数据流组合，实时绑定 `isFavorite` 状态，保证状态同步。

#### 问题4：应用重启后搜索文本丢失，无法恢复历史状态
- 现象：退出并重启应用，搜索框内容清空，不符合持久化需求；
- 解决方案：接入 Preferences DataStore，在文本变更时实时写入，ViewModel 初始化时读取并恢复文本；额外判断文本长度，若为3位IATA代码，自动选中对应机场并加载航班。

#### 问题5：CROSS JOIN 生成自身航线（出发=到达），出现无效数据
- 现象：航班列表中出现“机场→自身”的无效航线；
- 解决方案：在 `CROSS JOIN` 后增加过滤条件 `destination.iata_code != :departureCode`，排除自身航线。

#### 问题6：Flow 数据流重复触发查询，频繁刷新UI
- 现象：搜索文本轻微变化（如连续输入），多次执行数据库查询；
- 解决方案：对 `searchText` 增加 `debounce(250)` 防抖 + `distinctUntilChanged` 去重，仅在文本稳定变化后触发搜索。

#### 问题7：Room 加载预置资产数据库失败
- 现象：`createFromAsset` 无法读取 `assets/database/flight_search.db`，应用无机场数据；
- 解决方案：检查 assets 目录层级，确保数据库文件路径与代码一致，同时关闭数据库导出（`exportSchema = false`），避免冲突。

---

## 四、实验总结
1. **Room 数据库**：掌握 Entity 实体映射、DAO 复杂 SQL 编写、`LIKE` 模糊查询、`JOIN` 多表联合查询，理解 Room 与 Flow 结合实现响应式数据库的优势；
2. **DataStore 持久化**：理解新一代偏好存储的使用场景、异步读写与数据流特性，完成非结构化文本数据持久化；
3. **状态管理**：熟练使用 ViewModel + Flow + StateFlow 实现分层数据流、状态组合、防抖优化，实现 UI 单一状态驱动；
4. **Compose 界面**：基于状态分支与 `Crossfade` 实现多界面切换，掌握组件复用、交互事件回调的开发模式；
5. **工程优化**：针对卡顿、状态不同步、重复查询等问题完成性能与体验优化，理解移动端“数据层-逻辑层-UI层”分层架构思想。