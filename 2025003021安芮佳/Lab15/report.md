# Lab15 Flight Search 航班查询应用实验报告
## 一、实验概述
本实验基于 Android Jetpack Compose + Room 本地数据库开发航班查询App，实现机场模糊搜索、航线收藏、热门机场排序功能。通过 Entity 实体映射数据表、DAO 封装SQL查询、联合多表、模糊匹配等数据库技术完成本地数据持久化与业务查询逻辑。

## 二、数据表 Entity 实体设计说明
### 1. Airport 机场实体
对应本地数据库 `airport` 机场信息表，存储全部机场基础数据。
| 字段名 | 数据类型 | 约束与作用 |
|--------|----------|------------|
| id | Integer | 主键，唯一标识单条机场记录 |
| iata_code | String | 3位标准IATA机场编码，用于航线关联匹配 |
| name | String | 机场完整中文/英文名称，展示给用户 |
| passengers | Integer | 年度旅客吞吐量，用于热门机场排序 |

### 2. Favorite 收藏航线实体
对应 `favorite` 收藏表，记录用户收藏的出发-到达航线。
| 字段名 | 数据类型 | 约束与作用 |
|--------|----------|------------|
| id | Integer | 自增主键，收藏记录唯一标识 |
| departure_code | String | 出发机场IATA编码，关联Airport表 |
| destination_code | String | 目的地机场IATA编码，关联Airport表 |

## 三、DAO 数据访问接口查询方法设计
### 1. 基础数据加载
`getAllAirports()`
- 返回 Flow 流式机场列表，页面初始化时加载全部机场基础数据，支持数据自动刷新。

### 2. 机场模糊搜索（核心自动补全）
`searchAirports(String searchQuery)`
- 使用 `LIKE` 模糊匹配，同时检索 `iata_code`、`name` 双字段；
- 结果按 `passengers` 乘客量**降序**排序，优先展示热门机场，优化搜索推荐优先级。

### 3. 目的地筛选查询
`getDestinationsForAirport(String departCode)`
- 根据传入出发机场IATA编码，查询所有可到达目的地，自动排除出发机场自身。

### 4. 收藏航线完整信息联合查询
`getFavoriteFlights()`
- 多表INNER JOIN联合查询，同时读取收藏编码与机场名称；
- 一次性返回「收藏ID、出发编码、目的地编码、出发机场名、目的地机场名」完整展示数据，避免多次单表查询。

### 5. 收藏状态校验
`isFavorite(String departCode, String destCode)`
- 根据航线两端IATA编码查询，判断该航线是否已加入收藏，用于页面收藏按钮状态切换。

### 6. 收藏增删操作
- `addFavorite(Favorite flight)`：插入新航线收藏记录；
- `removeFavorite(int favoriteId)`：根据收藏主键删除指定航线。

## 四、SQL LIKE 模糊匹配关键字原理与应用
### 1. 使用语法
标准SQL模糊查询语法：
```sql
WHERE 字段名 LIKE '匹配模板'
```
本项目模板写法：`%$searchQuery%`
- `%` 为通配符，代表**任意长度任意字符（包含空字符）**；
- 前后包裹搜索关键词，实现**全包含匹配**。

### 2. 业务作用
1. 支持模糊输入：仅输入部分字母/文字，即可匹配包含该片段的机场名、IATA编码；
2. 双字段同时检索，兼顾编码快捷搜索与中文名称检索；
3. 提升交互体验，无需用户输入完整名称即可完成机场筛选。

## 五、多表联合查询（INNER JOIN）实现与业务价值
### 1. 完整SQL实现
```sql
SELECT f.id, f.departure_code, f.destination_code, 
       dep.name as departure_name, dest.name as destination_name
FROM favorite f
INNER JOIN airport dep ON f.departure_code = dep.iata_code
INNER JOIN airport dest ON f.destination_code = dest.iata_code
```
### 2. 实现逻辑
1. 以 `favorite` 收藏表为主表；
2. 第一次INNER JOIN关联 `airport` 表（别名dep），通过出发IATA编码获取出发机场名称；
3. 第二次INNER JOIN关联 `airport` 表（别名dest），通过目的地IATA编码获取目的地机场名称；
4. 使用别名 `departure_name`、`destination_name` 区分两个机场名称字段，直接返回页面可展示文本。

### 3. 作用与优势
1. **单次查询获取完整展示数据**：无需先查收藏表、再循环逐个查机场表，减少数据库IO开销；
2. 解决外键仅存编码、页面需要展示文字名称的业务痛点；
3. INNER JOIN 自动过滤无效收藏（对应机场已删除的脏数据），保证列表数据有效性。

## 六、整体业务流程总结
1. 应用启动：`getAllAirports()` 加载全部机场缓存；
2. 搜索页面：输入文字调用 `searchAirports()` 模糊匹配热门机场；
3. 航线选择：选定出发机场后，`getDestinationsForAirport()` 筛选合法目的地；
4. 收藏功能：`addFavorite` / `removeFavorite` 管理收藏，`isFavorite` 控制UI状态；
5. 收藏页面：`getFavoriteFlights()` 联合查询一次性加载带机场名称的收藏列表，渲染页面。