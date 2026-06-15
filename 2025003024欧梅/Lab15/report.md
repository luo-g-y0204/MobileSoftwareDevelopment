# Lab15 Flight Search 航班搜索实验报告
## 1. Entity 实体类设计说明
项目定义两张数据表对应的实体：
1. Airport：映射数据库 airport 表，字段 id（主键）、iata_code（机场三字码）、name（机场全称）、passengers（年客流量），用于搜索联想、航班展示；
2. Favorite：映射 favorite 收藏表，仅存储出发、到达机场IATA代码，不冗余存储机场名称；
额外创建 FavoriteRoute 数据类，用于多表联合查询后封装完整航线信息，一次性带出出发、到达机场名称，减少多次数据库查询。

## 2. DAO 接口查询方法设计
1. 自动补全搜索方法 searchAirport：接收模糊查询参数，使用 LIKE 匹配 iata_code 和 name，按 passengers 降序排序，优先展示客流量高的机场，实现输入实时联想；
2. getDestinations：传入出发机场代码，查询所有 IATA 不等于自身的机场，模拟所有可飞航班；
3. getAllFavorites：使用两次 INNER JOIN 关联 favorite 与两张 airport 表，联合查询获取收藏航线完整机场信息；
4. isFavorite：统计航线收藏记录数量，判断当前航班是否已收藏；
5. addFavorite / removeFavorite：新增、删除收藏记录，提供收藏功能底层操作。

## 3. SQL LIKE 关键字作用与使用方式
使用语法 `WHERE iata_code LIKE :key OR name LIKE :key`，传入参数拼接前后百分号 `%搜索内容%`；
百分号代表任意长度任意字符，实现模糊检索，用户仅输入片段字符即可匹配包含该字符的机场，实现输入实时自动补全，满足实验搜索需求。

## 4. 多表联合查询实现与作用
favorite 表仅保存机场三字码，无法直接展示机场名称；
通过 `favorite INNER JOIN airport AS departure ON departure_code = departure.iata_code INNER JOIN airport AS destination ON destination_code = destination.iata_code` 一次查询同时关联两张机场表，同时拿到出发、到达机场完整名称；
优势：单次数据库IO获取全部展示数据，避免循环遍历收藏列表重复查询机场信息，提升查询性能。

## 5. Preferences DataStore 使用场景与实现
### 使用场景
1. 持久保存用户搜索框输入文本，重启App自动回填上次搜索内容；
2. 搜索框为空时页面自动切换至收藏列表，状态持久化；
3. 相比 SharedPreferences 支持 Flow 异步数据流，配合 Compose 自动响应更新UI。
### 实现逻辑
定义字符串存储键 SEARCH_TEXT，提供 saveSearch() 挂起函数写入存储，searchTextFlow 数据流实时监听本地存储值，ViewModel 初始化时同步读取本地缓存回填搜索框。

## 6. ViewModel 状态管理设计
1. 内部使用 MutableStateFlow 管理搜索文本状态，修改文本时同步写入 DataStore；
2. 将数据库查询 Flow 通过 stateIn 缓存为 StateFlow，避免重复查询数据库；
3. 封装全部数据库、DataStore 业务逻辑，UI层仅调用暴露的方法，实现数据层与界面解耦；
4. 屏幕旋转、页面重建时，ViewModel 保留所有查询流与UI状态，无需重复加载数据。

## 7. UI 界面切换逻辑
页面使用单一 Scaffold + Box 分层容器，通过判断搜索文本、选中机场变量实现三种视图切换：
1. 搜索框有文字：展示自动补全机场 LazyColumn 列表，点击条目选中出发机场；
2. 搜索框空白：仅展示收藏航线列表；
3. 已选中出发机场：展示该机场全部可飞航班列表，每条航班附带收藏切换按钮；
所有列表均使用 LazyColumn 懒加载，优化内存占用。

## 8. 实验过程遇到的问题与解决方案
1. Gradle 版本、Kotlin、Compose 编译器版本不匹配，项目编译失败
解决：统一配套 AGP、Gradle、Kotlin、Compose Compiler 版本，严格遵循官方版本对应表；
2. KSP 插件找不到、依赖缺失报错
解决：在根 build.gradle 声明KSP插件版本，app模块添加KSP插件，补充Room编译依赖；
3. collectAsStateWithLifecycle 无法识别
解决：补充 lifecycle-runtime-compose 依赖，文件头部导入对应包；
4. 包路径层级错误，导入类提示 Unresolved reference
解决：统一每个文件顶部 package 路径，文件夹层级与代码包名完全对应；
5. Room 预置 assets 数据库加载失败
解决：将 flight_search.db 放置在 src/main/assets/database 目录，Database 类使用 createFromAsset 加载本地库；
6. LIKE 查询无匹配结果
解决：传入参数手动拼接前后 %，仅原始文本无法实现模糊匹配；
7. 重启App搜索文本丢失
解决：ViewModel 初始化同步读取 DataStore 存储值，回填搜索文本状态。