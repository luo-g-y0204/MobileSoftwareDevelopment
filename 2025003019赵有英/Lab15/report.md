# Lab15 FlightSearch 实验报告
**学号：2025003019**
**姓名：赵有英**

## 1. Entity实体类设计说明
1. Airport
映射数据库airport表，主键id；iata_code、name、passengers分别对应机场三字码、名称、年客流量；@ColumnInfo匹配数据库下划线列名与kotlin驼峰变量。
2. Favorite
映射favorite收藏表，id自增主键；departure_code、destination_code存储出发/目的地三字码，仅存代码，名称靠联表查询获取。

## 2. DAO查询方法设计
1. searchAirports：LIKE模糊匹配iata_code与name，按客流量降序，实现输入自动补全；
2. getDestinations：过滤自身机场，查询所有可飞往目的地；
3. getAllFavoritesWithName：双INNER JOIN联表，把收藏代码替换成完整机场名称；
4. 增删查收藏：getFavoriteByRoute精准匹配航线、insertFavorite新增、deleteFavorite删除收藏记录。

## 3. SQL LIKE关键字作用与用法
LIKE是模糊匹配运算符，`%`为通配符；`'%' || :query || '%'`包裹输入内容，匹配包含输入字符的机场名称/三字码，实现实时搜索联想。Room中用||拼接字符串与参数，防止注入问题。

## 4. 联合查询实现与作用
对favorite表分别别名departure、destination关联两次airport表，分别取出出发、目的地机场名称。
作用：收藏表只存代码，联表一次性拿到完整展示信息，不用多次单独查Airport，减少IO查询、提升列表加载性能。

## 5. Preferences DataStore实现与场景
场景：持久保存用户搜索框文本，重启应用自动回填文字；空文字则默认展示收藏页。
实现：
1. 定义全局Context扩展dataStore实例；
2. stringPreferencesKey创建唯一存储键；
3. saveSearchText挂起函数，edit写入字符串；
4. savedSearchTextFlow流式读取，默认空字符串；
5. ViewModel初始化协程读取历史文字，输入变更时自动调用保存。

## 6. ViewModel状态管理设计
1. 基础UI状态：searchText、selectedDepartAirport用mutableStateOf，Compose自动监听刷新；
2. 数据库数据流：snapshotFlow把Compose状态转Flow，flatMapLatest输入变化自动重查数据库；stateIn限定生命周期，页面销毁自动停止查询；
3. 收藏优化：预加载所有收藏航线字符串集合，O(1)判断是否收藏，避免列表每条都查库；
4. 所有数据库、DataStore操作全部包裹viewModelScope协程，内存安全、页面销毁自动取消任务。

## 7. UI界面切换逻辑
依靠when三分支判断：
1. selectedDepartAirport不为空 → 渲染目的地航班列表；
2. searchText非空、无选中机场 → 渲染搜索自动补全机场列表；
3. searchText为空 → 渲染收藏航班列表；
全部状态由ViewModel单向驱动，界面无业务逻辑，纯展示层。

## 8. 实验问题与解决过程
1. KSP版本不匹配编译报错：核对Kotlin2.2.10对应KSP2.2.10-2.0.2，修正版本号同步成功；
2. AGP9.x禁止kotlin.sourceSets报错：gradle.properties添加android.disallowKotlinSourceSets=false兼容KSP；
3. Kotlin2.0缺少Compose编译器插件：toml添加compose-compiler插件，根目录与app模块分别apply；
4. 主题资源找不到：res/values新建themes.xml补充Theme.FlightSearch基础无ActionBar主题；
5. Room2.6.1与KSP2签名冲突：升级Room至2.7.0适配KSP2架构；
6. 收藏按钮状态不同步：预加载收藏Set集合，不用逐条查询，界面点击实时刷新状态。