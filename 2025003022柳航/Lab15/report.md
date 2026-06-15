# Lab15 Flight Search 航班查询应用 实验报告
**学号**：2025003022
**姓名**：柳航

## 一、Entity 实体类设计说明
本次实验根据预置数据库 `flight_search.db` 两张数据表，分别创建对应 Room 实体类，完成数据表与 Kotlin 类的映射。

### 1. Airport 实体类
对应数据库 `airport` 表，存储机场基础信息：
- `id`：整型主键，机场唯一标识
- `iata_code`：字符串，机场三位 IATA 编码
- `name`：字符串，机场全称
- `passengers`：整型，年客流量，用于自动补全结果排序

使用 `@Entity` 注解声明数据表，`@PrimaryKey` 标记主键，完成字段一一映射。

### 2. Favorite 实体类
对应数据库 `favorite` 表，存储用户收藏航班航线：
- `id`：整型主键，收藏记录唯一标识
- `departure_code`：字符串，出发机场 IATA 编码
- `destination_code`：字符串，目的机场 IATA 编码

仅存储机场编码，机场名称通过后续**联合查询**从 `airport` 表获取，减少数据冗余。

## 二、DAO 数据访问接口设计说明
在 `FlightDao` 中定义所有数据库增删改查方法，基于 Room 注解 + SQL 语句实现业务查询，所有查询通过 Flow 异步返回数据，适配 Compose 状态监听。

### 1. 机场自动补全查询
接收用户输入关键词，使用 `LIKE` 模糊匹配 `iata_code` 和 `name` 字段，结合 `%` 通配符实现模糊搜索；结果按照 `passengers` 客流量**降序**排列，优先展示热门机场。

### 2. 航班列表查询
根据选中的出发机场 IATA 编码，查询数据库内**除自身外**所有机场作为目的地，模拟“该机场飞往所有其他机场”的业务逻辑。

### 3. 收藏航线查询
使用 **INNER JOIN** 多表联合查询，关联 `favorite` 表与两次 `airport` 表，通过机场编码匹配，同时取出出发、目的地机场的编码与名称，补全收藏列表展示信息。

### 4. 收藏增删方法
提供添加收藏、删除收藏两条操作方法，实现航线收藏/取消收藏功能，操作直接作用于 `favorite` 数据表。

## 三、SQL LIKE 关键字使用说明
### 1. 关键字作用
`LIKE` 是 SQL 标准的**模糊查询关键字**，区别于 `=` 精准匹配，它支持对字符串字段进行**部分内容匹配**，专门用于搜索、输入联想、内容检索等场景。
本实验中用户输入机场简称、部分 IATA 编码、机场名称片段时，无法使用精准匹配查询全部相关机场，因此采用 `LIKE` 实现实时自动补全联想功能，提升搜索灵活性。

### 2. 通配符规则与实现方式
`LIKE` 必须搭配通配符 `%` 使用，`%` 代表**任意长度的任意字符（包含空字符）**，常用组合规则：
1. `关键词%`：匹配**以关键词开头**的内容；
2. `%关键词`：匹配**以关键词结尾**的内容；
3. `%关键词%`：匹配**任意位置包含关键词**的内容，本次实验采用该规则。

结合实验需求，用户可能输入 IATA 编码片段、机场名称片段，无法限定匹配位置，因此在代码中将用户输入字符串**前后拼接 `%`**，组合成 `"%$inputText%"` 格式后传入 SQL 语句。
同时检索 `iata_code`（机场三字码）和 `name`（机场全称）两个字段，只要任意一个字段包含输入内容，就判定为匹配成功。

### 3. 完整 SQL 语句与逻辑解析
```sql
SELECT * FROM airport
WHERE iata_code LIKE :search OR name LIKE :search
ORDER BY passengers DESC
```
- `SELECT * FROM airport`：查询 `airport` 表中**所有字段**数据；
- `WHERE`：查询条件筛选入口；
- `iata_code LIKE :search`：对机场三字码字段执行模糊匹配，`:search` 是 Room 中的**命名参数**，用于接收代码中拼接好 `%` 的搜索字符串；
- `OR`：逻辑或，满足「编码匹配」**或者**「名称匹配」任一条件即可命中；
- `name LIKE :search`：对机场名称字段执行模糊匹配；
- `ORDER BY passengers DESC`：对匹配后的结果集进行排序，`passengers` 为年客流量字段，`DESC` 表示**降序排列**。

排序逻辑：客流量越高代表机场越热门，自动补全列表优先展示热门机场，符合常规使用习惯。

### 4. Room 代码配套写法
在 DAO 接口中配合注解实现该查询，示例如下：
```kotlin
@Query("SELECT * FROM airport WHERE iata_code LIKE :search OR name LIKE :search ORDER BY passengers DESC")
fun searchAirport(search: String): Flow<List<Airport>>
```
调用处拼接通配符：
```kotlin
val searchStr = "%${userInput}%"
flightDao.searchAirport(searchStr)
```

## 四、联合查询实现与作用
### 1. 使用场景
`favorite` 表仅存储机场 IATA 编码，无机场名称，无法直接展示完整航线信息，因此使用**多表联合查询**关联两张数据表。

### 2. 实现逻辑
将 `favorite` 表分别与 `airport` 表做两次关联：
1. 关联 `departure_code` 获取出发机场信息
2. 关联 `destination_code` 获取目的机场信息

### 3. 核心语句
```sql
SELECT * FROM favorite
INNER JOIN airport AS departure ON favorite.departure_code = departure.iata_code
INNER JOIN airport AS destination ON favorite.destination_code = destination.iata_code
```

### 4. 作用
- 减少数据表冗余，无需在收藏表重复存储机场名称
- 一次查询获取完整航线数据，提升查询效率

## 五、Preferences DataStore 使用说明
### 1. 使用场景
替代传统 SharedPreferences，轻量持久化存储**用户搜索文本**，实现应用重启后状态保留：
1. 退出应用时保存当前搜索框内容
2. 重启应用自动回填搜索文本
3. 搜索框为空时，默认展示收藏列表

### 2. 核心实现
1. 定义字符串偏好键 `search_text`，用于存储搜索内容
2. 封装 `UserPreferencesRepository` 工具类，统一管理存储与读取逻辑
3. 写入：通过 `dataStore.edit{}` 在协程中更新偏好数据
4. 读取：将 DataStore 数据转为 Flow，供 ViewModel 监听、UI 刷新

### 3. 优势
基于 Flow 实现可观察数据，配合 Compose 生命周期自动监听，数据更新实时反馈到界面。

## 六、ViewModel 状态管理设计
`FlightViewModel` 作为 UI 层与数据层的中间层，统一管理整个应用 UI 状态与业务逻辑，生命周期跟随页面，防止屏幕旋转数据丢失。

1. **搜索文本状态**：接收 DataStore 读取的历史搜索内容，双向绑定搜索框输入
2. **自动补全状态**：监听搜索文本变化，实时调用 DAO 查询机场列表，更新下拉建议
3. **航班列表状态**：选中机场后，查询对应航班数据并更新列表
4. **收藏列表状态**：搜索框为空时，加载所有收藏航线
5. **收藏操作**：接收 UI 点击事件，调用 DAO 完成添加/删除收藏，并刷新列表

所有数据使用 `StateFlow` 对外暴露，Compose 通过 `collectAsState()` 收集状态驱动 UI。

## 七、UI 界面切换逻辑说明
基于 Jetpack Compose + LazyColumn 实现多视图切换，整体逻辑如下：

1. **初始状态**：搜索框无内容，页面展示**收藏航线列表**
2. **输入状态**：搜索框输入文字，下方弹出**自动补全机场建议列表**
3. **选中机场**：点击补全项，隐藏建议列表，页面切换为**航班列表**
4. **航班条目**：每条航班展示双机场信息，搭配收藏按钮，点击可收藏/取消
5. **清空搜索框**：搜索文本置空，自动切回**收藏列表**

布局使用 Box 层叠结构，实现输入框与补全列表的层级展示，结合 Compose 动画提升交互体验。

## 八、实验问题与解决过程
1. **预置数据库加载失败**
问题：放入 `assets/database/` 目录后无法读取数据库。
解决：在 Room 数据库类中使用 `createFromAsset("database/flight_search.db")` 指定资源路径，修正目录层级后正常加载。

2. **LIKE 模糊查询无结果**
问题：直接传入用户输入内容，无法模糊匹配。
解决：按照要求手动拼接 `%关键词%` 通配符，再传入 SQL 查询语句。

3. **航班出现自身到自身的航线**
问题：查询航班时包含出发机场本身。
解决：在 SQL 语句中增加判断，过滤 `iata_code` 与出发编码相同的数据。

4. **DataStore 写入无效果**
问题：退出应用后搜索文本未保存。
解决：DataStore 操作必须在**协程**中执行，在 ViewModel 内调用 `viewModelScope` 启动协程完成存储。

5. **界面状态旋转屏幕丢失**
问题：横竖屏切换后列表、输入内容重置。
解决：使用 ViewModel + StateFlow 托管所有 UI 状态，ViewModel 不因屏幕旋转销毁，保留数据。
```