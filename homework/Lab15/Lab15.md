# Lab15：Flight Search 应用项目

## 实验背景

本次实验要求构建 Flight Search 应用，用户可以输入机场并查看以该机场作为出发地的目的地列表。应用需要查询预先填充的数据库，显示航班列表，并允许用户保存收藏航班。

---

## 前提条件

- 具备读取和操控关系型数据库的 SQL 基础知识
- 能够在 Android 应用中使用 Room 对数据库执行读写操作
- 能够使用 DataStore 存储简单的数据
- 能够使用 Compose 构建中等复杂的界面

---

## 实验目标

完成本实验后，你应能够：

- 使用 Room 读取和写入关系型数据库
- 使用 Preferences DataStore 保存用户状态
- 实现数据库查询的自动补全功能
- 使用 SQL `LIKE` 关键字进行文字搜索
- 实现数据库联合查询
- 在 Compose 中构建中等复杂的界面
- 管理应用的 UI 状态

---

## 应用要求

### 核心功能

1. **搜索机场**
   - 提供文本字段，供用户输入机场名称或 IATA 机场标识符
   - 在用户输入时查询数据库提供自动补全建议

2. **显示航班列表**
   - 当用户选择建议时，生成从该机场出发的航班列表
   - 每个航班条目包含出发地和目的地的 IATA 代码和机场名称
   - 每个条目提供保存到收藏夹的按钮

3. **收藏功能**
   - 让用户能够保存个人喜欢的路线
   - 未输入搜索查询时，显示用户选择的所有收藏航线

4. **状态保持**
   - 使用 Preferences DataStore 保存搜索文本
   - 重新打开应用时预填充搜索文本

---

## 数据库结构

### airport 表

| 列 | 数据类型 | 说明 |
|---|---|---|
| id | INTEGER | 唯一标识符（主键）|
| iata_code | VARCHAR | IATA 代码（3 个字母）|
| name | VARCHAR | 机场全称 |
| passengers | INTEGER | 每年的乘客人数 |

### favorite 表

| 列 | 数据类型 | 说明 |
|---|---|---|
| id | INTEGER | 唯一标识符（主键）|
| departure_code | VARCHAR | 出发地的 IATA 代码 |
| destination_code | VARCHAR | 目的地的 IATA 代码 |

### 获取数据库

数据库文件 `flight_search.db` 已包含在本目录中，无需额外下载。

---

## 界面规划

### 初始界面
- 空界面，带文本字段提示输入机场

### 自动补全界面
- 用户开始输入时，显示匹配机场名称或标识符的自动补全建议列表

### 航班列表界面
- 用户选择建议后，显示从该机场出发的所有可能航班
- 每个条目包含：两个机场的标识符和名称、保存到收藏夹的按钮

### 收藏列表界面
- 搜索框为空时，显示已保存的收藏目的地列表

**提示：** 使用 `LazyColumn` 显示列表。可以将布局封装在 Box 中，使用动画 API 在搜索结果列表前显示自动补全建议。

---

## 数据库查询要求

### 1. 自动补全查询

在 airport 表中搜索自动补全建议：
- 对照 `iata_code` 列和 `name` 列检查用户输入
- 使用 `LIKE` 关键字执行文字搜索
- 按 `passengers` 列降序排序，优先显示更频繁访问的机场

### 2. 航班查询

假设每个机场都有飞往数据库中其他所有机场的航班（自身除外）

### 3. 收藏查询

搜索框为空时，显示收藏航班列表：
- favorite 表仅包含机场代码列
- 需要联合查询显示机场名称

### 4. 查询性能

使用 SQL 和 Room API 执行所有查询，只根据需要检索所需数据（避免一次性加载整个数据库）

---

## Preferences DataStore 要求

- 将用户的搜索字符串存储在 Preferences DataStore 中
- 用户重新启动应用时，用存储的搜索文本预填充文本字段
- 如果用户退出应用时文本字段为空，重新打开时显示收藏航班列表

---

## 实验任务

### 任务一：项目初始化

1. 创建新的 Android 项目
2. 添加 Room 依赖和 KSP 编译器依赖
3. 添加 DataStore 依赖
4. 将本目录中的 `flight_search.db` 放置到项目的 `assets/database/` 目录

### 任务二：创建数据层

1. 创建 `Airport` 和 `Favorite` Entity 类
2. 创建 DAO 接口，包含：
   - 查询所有机场的方法
   - 根据名称或代码搜索机场的方法
   - 查询特定机场的所有目的地的方法
   - 查询所有收藏航线的方法
   - 添加收藏航线的方法
   - 删除收藏航线的方法
3. 创建 Room Database 类
4. 使用 `createFromAsset()` 从预置数据库初始化

### 任务三：实现 DataStore

1. 创建 Preferences DataStore 实例
2. 实现保存搜索文本的方法
3. 实现读取搜索文本的方法

### 任务四：实现 ViewModel

1. 创建 ViewModel 管理 UI 状态
2. 实现搜索文本状态管理
3. 实现自动补全建议查询
4. 实现航班列表查询
5. 实现收藏列表查询
6. 实现添加/删除收藏功能

### 任务五：构建 UI

1. 创建搜索文本输入框
2. 实现自动补全建议列表
3. 实现航班列表显示
4. 实现收藏列表显示
5. 添加收藏/取消收藏按钮
6. 实现界面切换逻辑

### 任务六：测试验证

测试以下功能：
- 搜索框输入时显示自动补全建议
- 选择机场后显示航班列表
- 航班列表按正确顺序显示
- 可以添加和删除收藏
- 搜索框为空时显示收藏列表
- 重启应用后搜索文本保持
- 旋转屏幕后状态保持

---

## 关键实现点

### SQL LIKE 查询示例

```sql
SELECT * FROM airport
WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery
ORDER BY passengers DESC
```

### 联合查询示例

```sql
SELECT * FROM favorite
INNER JOIN airport AS departure ON favorite.departure_code = departure.iata_code
INNER JOIN airport AS destination ON favorite.destination_code = destination.iata_code
```

### DataStore 使用示例

```kotlin
val SEARCH_TEXT = stringPreferencesKey("search_text")

suspend fun saveSearchText(text: String) {
    dataStore.edit { preferences ->
        preferences[SEARCH_TEXT] = text
    }
}

val searchTextFlow: Flow<String> = dataStore.data
    .map { preferences ->
        preferences[SEARCH_TEXT] ?: ""
    }
```

---

## 提交要求

在自己的文件夹下新建 `Lab15/` 目录，提交以下内容：

```text
学号姓名/
└── Lab15/
    ├── Airport.kt
    ├── Favorite.kt
    ├── FlightDao.kt
    ├── FlightDatabase.kt
    ├── UserPreferencesRepository.kt
    ├── FlightViewModel.kt
    ├── FlightScreen.kt
    ├── screenshot_search.png
    ├── screenshot_flights.png
    ├── screenshot_favorites.png
    └── report.md
```

`report.md` 需包含：

1. Entity 设计说明（airport 和 favorite 表映射）
2. DAO 查询方法设计说明（自动补全、航班查询、收藏查询）
3. `LIKE` 关键字的使用方法和作用
4. 联合查询的实现和作用
5. Preferences DataStore 的使用场景和实现
6. ViewModel 状态管理设计
7. UI 切换逻辑说明
8. 实验中遇到的问题与解决过程

---

## 验收标准

满足以下条件可视为完成实验：

- 应用可正常编译和运行
- 添加了正确的 Room 和 DataStore 依赖
- 正确创建了 `Airport` 和 `Favorite` Entity
- DAO 包含所有必需的查询方法
- 实现了自动补全功能
- 实现了航班列表显示
- 实现了收藏功能
- 使用 DataStore 保存搜索文本
- 应用重启后搜索文本保持
- UI 界面符合要求
- 报告清晰说明实现思路

---

## 提示

- 使用 `LIKE` 时需要在参数前后添加 `%`：`"%$searchQuery%"`
- 自动补全建议应该排除已选择的机场
- 航班查询时要排除目的地与出发地相同的情况
- 收藏按钮状态应根据当前航线是否已收藏来显示
- 使用 Flow 和 StateFlow 管理异步数据
- 使用 `collectAsState()` 在 Compose 中收集 Flow
- DataStore 操作需要在协程中执行

---

## 截止时间

**2026-06-16**，届时关于 Lab15 的 PR 请求将不会被合并。
