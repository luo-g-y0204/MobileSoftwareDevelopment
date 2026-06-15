# Lab15 Flight Search 实验报告

## 一、实验名称

基于 Room、Preferences DataStore 与 Jetpack Compose 的 Flight Search 航班搜索应用

## 二、实验目的

1. 掌握 Room 对预置 SQLite 数据库的读取与写入。
2. 掌握使用 SQL `LIKE` 实现机场名称和 IATA 代码的模糊搜索。
3. 掌握使用 `JOIN` 查询补全收藏航线的机场名称。
4. 掌握 Preferences DataStore 保存简单用户状态的方法。
5. 掌握 ViewModel、Flow 和 StateFlow 在 Compose 中的状态管理。
6. 完成机场自动补全、航班列表、收藏和状态恢复功能。

## 三、Entity 设计说明

### 1. Airport 实体

`Airport.kt` 使用 `@Entity(tableName = "airport")` 映射数据库中的 `airport` 表。

| Kotlin 属性 | 数据库列 | 类型 | 说明 |
|---|---|---|---|
| `id` | `id` | Int | 自增主键 |
| `name` | `name` | String | 机场全称 |
| `iataCode` | `iata_code` | String | 三位 IATA 代码 |
| `passengers` | `passengers` | Int | 年客流量 |

其中 `iataCode` 通过 `@ColumnInfo(name = "iata_code")` 映射下划线命名的数据库列。

### 2. Favorite 实体

`Favorite.kt` 使用 `@Entity(tableName = "favorite")` 映射 `favorite` 表。

| Kotlin 属性 | 数据库列 | 类型 | 说明 |
|---|---|---|---|
| `id` | `id` | Int | 自增主键 |
| `departureCode` | `departure_code` | String | 出发机场代码 |
| `destinationCode` | `destination_code` | String | 目的地机场代码 |

收藏表只保存两个机场代码，不重复保存机场名称，避免数据冗余。

## 四、DAO 查询方法设计

### 1. 查询所有机场

```sql
SELECT * FROM airport
ORDER BY passengers DESC
```

按照客流量降序返回机场，满足实验要求。

### 2. 自动补全查询

```sql
SELECT * FROM airport
WHERE iata_code LIKE '%' || :query || '%' COLLATE NOCASE
   OR name LIKE '%' || :query || '%' COLLATE NOCASE
ORDER BY passengers DESC
LIMIT 10
```

该查询同时搜索 IATA 代码和机场名称，使用 `COLLATE NOCASE` 忽略英文大小写，并通过 `LIMIT 10` 避免一次读取过多数据。

### 3. 航班查询

题目假设每个机场都可以飞往其他所有机场，因此使用同一张 `airport` 表的交叉连接：

```sql
SELECT
    departure.iata_code AS departure_code,
    departure.name AS departure_name,
    destination.iata_code AS destination_code,
    destination.name AS destination_name
FROM airport AS departure
CROSS JOIN airport AS destination
WHERE departure.iata_code = :departureCode
  AND destination.iata_code != :departureCode
ORDER BY destination.passengers DESC
```

`destination.iata_code != :departureCode` 用于排除机场飞往自身的情况。

### 4. 收藏查询

```sql
SELECT
    favorite.id,
    favorite.departure_code,
    departure.name AS departure_name,
    favorite.destination_code,
    destination.name AS destination_name
FROM favorite
INNER JOIN airport AS departure
    ON favorite.departure_code = departure.iata_code
INNER JOIN airport AS destination
    ON favorite.destination_code = destination.iata_code
```

收藏表只有代码，因此分别把 `airport` 表别名为 `departure` 和 `destination`，通过两次联合查询得到两个机场的完整名称。

### 5. 添加与删除收藏

添加前调用 `isFavorite()` 判断航线是否已收藏，防止用户连续点击产生重复数据。删除时根据出发代码和目的地代码删除对应记录。

## 五、LIKE 关键字的使用与作用

`LIKE` 用于模糊匹配文字。`%` 表示任意数量的字符，因此：

```sql
name LIKE '%' || :query || '%'
```

表示机场名称中只要包含用户输入内容就匹配。例如输入 `London` 可以匹配名称中包含 `London` 的机场；输入 `LA` 也能匹配包含 `LA` 的 IATA 代码。使用 `COLLATE NOCASE` 后，输入大小写不影响结果。

## 六、联合查询的实现与作用

`favorite` 表只保存机场代码，界面却需要显示机场名称。通过 `INNER JOIN` 把 `favorite.departure_code` 和 `favorite.destination_code` 分别连接到 `airport.iata_code`，即可在一次 SQL 查询中返回完整航线信息。这样既保持数据库规范化，又避免在应用层逐条查询机场名称。

## 七、Preferences DataStore 的使用

DataStore 保存搜索框文本，键名为：

```kotlin
val SEARCH_TEXT = stringPreferencesKey("search_text")
```

保存时使用：

```kotlin
dataStore.edit { preferences ->
    preferences[SEARCH_TEXT] = text
}
```

读取时把 DataStore 数据转换为 `Flow<String>`。应用启动后，ViewModel 读取保存内容并填入搜索框。如果保存内容是完整的三位 IATA 代码，则进一步查询机场并恢复航班列表；如果为空，则显示收藏列表。

## 八、ViewModel 状态管理设计

`FlightViewModel` 维护以下状态：

- `searchText`：搜索框文字。
- `selectedAirport`：用户选择的机场。
- `suggestions`：自动补全机场列表。
- `flights`：选择机场后的航班列表。
- `favorites`：收藏航线列表。
- `isLoading`：DataStore 初始读取状态。

搜索文字变化后使用 `debounce(250)` 延迟 250 毫秒再查询，减少快速输入时的数据库请求。`flatMapLatest` 会取消旧查询，只保留最新输入对应的结果。航班列表与收藏键集合使用 `combine` 合并，从而实时显示每条航线是否已收藏。

ViewModel 本身可以跨屏幕旋转保留，搜索文本还会写入 DataStore，因此应用进程重启后也能恢复。

## 九、UI 切换逻辑

界面根据状态切换为四种模式：

1. DataStore 尚未读取完成：显示加载动画。
2. 搜索框为空：显示收藏列表。
3. 搜索框非空但尚未选择机场：显示自动补全列表。
4. 已选择机场：显示从该机场出发的所有航班。

列表全部使用 `LazyColumn`，避免同时创建所有列表项。`Crossfade` 用于不同界面状态之间的平滑切换。航班右侧的爱心图标根据 `isFavorite` 显示实心或空心，并支持添加和取消收藏。

## 十、实验中遇到的问题与解决过程

### 问题 1：预置数据库版本为 0

检查数据库后发现 `PRAGMA user_version` 为 0，而 Room 的 `@Database(version = 1)` 需要数据库版本一致，否则首次打开会被判断为从 0 升级到 1，并要求提供迁移方案。

**解决方法：** 只在项目资产副本中执行 `PRAGMA user_version = 1`。机场数据、收藏表和表结构均未改变。

### 问题 2：收藏表没有机场名称

直接查询 `favorite` 只能得到代码，无法满足界面显示完整机场名称的要求。

**解决方法：** 对 `airport` 表使用两个别名并进行两次 `INNER JOIN`。

### 问题 3：搜索时数据库查询过于频繁

Compose 文本框每输入一个字符都会更新状态，若直接查询会产生大量短时间请求。

**解决方法：** 在 Flow 中增加 `debounce(250)` 和 `distinctUntilChanged()`，并使用 `flatMapLatest` 取消过时查询。

### 问题 4：航班列表中的收藏图标需要实时更新

添加收藏后，仅重新读取航班数据不能直接知道该航线是否收藏。

**解决方法：** 将航班查询结果与 `favorite` 表的 Flow 组合，把收藏记录转换为代码对集合，再生成带 `isFavorite` 字段的 UI 模型。

## 十一、实验结果

项目实现了以下功能：

- 机场名称和 IATA 代码自动补全。
- 按客流量排序搜索建议。
- 显示所选机场到其他所有机场的航班。
- 添加和删除收藏航线。
- 搜索框为空时显示收藏列表。
- 使用 DataStore 保存并恢复搜索文本。
- 使用 ViewModel 保持旋转屏幕后的 UI 状态。
- 使用 Room、Flow、StateFlow 与 Compose 完成数据驱动界面。

## 十二、实验总结

本实验把 Room 数据库、SQL 模糊查询和联合查询、DataStore 持久化、ViewModel 状态管理以及 Compose 列表界面组合到一个完整应用中。通过数据库 Flow 与 UI State 的组合，数据库内容发生变化后界面能够自动刷新。该实现避免一次性加载全部数据，并对搜索输入进行了防抖处理，满足实验的功能和性能要求。
