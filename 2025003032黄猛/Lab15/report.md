# Lab15 实验报告

## 1. Entity 设计说明

`Airport` 对应 `airport` 表，字段包含 `id`、`iata_code`、`name` 和 `passengers`。`Favorite` 对应 `favorite` 表，保存出发地和目的地的 IATA 代码，用于记录用户收藏的航线。

## 2. DAO 查询方法设计说明

`searchAirports()` 使用 `LIKE` 在 `iata_code` 和 `name` 中执行自动补全搜索，并按 `passengers` 降序排序。`observeRoutesFrom()` 查询指定出发机场到其他全部机场的航线。`observeFavoriteRoutes()` 使用联合查询把收藏表和机场表关联起来，返回可直接显示的航线详情。

## 3. `LIKE` 关键字的使用方法和作用

`LIKE` 用于模糊匹配字符串。本实验在查询时把用户输入包裹为 `"%关键字%"`，这样可以匹配机场代码或机场名称中包含该片段的记录。

## 4. 联合查询的实现和作用

收藏表只保存机场代码，因此展示收藏列表时需要把 `favorite` 表和 `airport` 表关联起来，查询出出发地和目的地的机场名称。这样 UI 不必自己再做二次查询。

## 5. Preferences DataStore 的使用场景和实现

DataStore 用于保存搜索框文本。应用启动后从 `searchTextFlow` 读取数据并恢复输入内容，用户每次修改搜索框时即时写回，保证重启后状态可恢复。

## 6. ViewModel 状态管理设计

ViewModel 维护搜索文本、自动补全建议、当前航班列表和收藏列表，并通过 `StateFlow` 统一暴露给 Compose。界面只负责渲染状态和触发事件。

## 7. UI 切换逻辑说明

当搜索框为空时显示收藏列表；当用户输入并选择机场后显示该机场的航班列表；当输入但尚未选择时显示自动补全建议。这样可以把搜索、结果和收藏三种状态清晰分离。

## 8. 实验中遇到的问题与解决过程

本实验的主要问题是预置数据库项目没有现成工程骨架，因此先搭建了可导入 Android Studio 的 Gradle 项目，再逐步补齐 Room、DataStore 和 Compose 层。随后通过统一的 ViewModel 状态管理把自动补全、航班展示和收藏功能串起来。
