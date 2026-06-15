# Lab15 Flight Search 实验报告
## 一、实验信息
实验名称：航班搜索应用 Flight Search
实验环境：Android Studio Hedgehog、Kotlin、Jetpack Compose、Room、DataStore
实验目标：实现机场搜索、自动补全、航班展示、航线收藏、状态持久化功能

## 二、Entity 实体设计说明
### 1. Airport 实体
对应数据库 `airport` 表，包含字段：
- id：自增主键，唯一标识机场
- iata_code：机场三字IATA编码
- name：机场全称
- passengers：年客流量，用于搜索结果排序
使用 `@Entity` 注解映射数据表，`@ColumnInfo` 映射数据库列名。

### 2. Favorite 实体
对应数据库 `favorite` 收藏表：
- id：自增主键
- departure_code：出发机场IATA编码
- destination_code：目的地机场IATA编码
用于存储用户收藏的航线组合。

## 三、DAO 查询方法设计说明
1. **自动补全查询**
使用 `LIKE` 模糊匹配 `iata_code` 和 `name`，并根据 `passengers` 客流量**降序排序**，优先展示热门机场，返回 Flow 实现实时监听。
2. **单机场查询**
根据 IATA 编码查询单个机场信息，使用 `suspend` 挂起函数在协程中执行。
3. **目的地查询**
查询除自身外所有机场，生成航班数据源。
4. **航班列表生成**
封装 Flow，遍历所有目的地，组装 `FlightRoute` 航班模型。
5. **收藏联表查询**
使用 `INNER JOIN` 联合 `favorite` 表与 `airport` 表，一次性查询收藏航线+机场名称，满足实验联表要求。
6. **收藏增删查**
包含判断是否收藏、新增收藏、删除收藏的基础数据库操作。

## 四、SQL LIKE 关键字使用
### 作用
`LIKE` 用于模糊查询，实现输入关键词匹配机场编码/名称，完成自动补全功能。
### 使用方式
结合通配符 `%` 使用：`"%$text%"`，表示**包含该关键词的所有结果**。
SQL 语句：
```sql
SELECT * FROM airport WHERE iata_code LIKE :query OR name LIKE :query ORDER BY passengers DESC