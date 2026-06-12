# Lab14 实验报告
## 一、Entity、DAO、Database 职责
1. **Entity**：将 `BusSchedule` 数据类与数据库 `Schedule` 表做映射，定义表结构、字段与主键，实现对象和数据库记录的绑定。
2. **DAO**：封装数据库查询操作，提供获取全部数据、按站点查询数据的接口，隔离 SQL 细节。
3. **Database**：Room 数据库主类，配置数据表与版本，以单例模式管理数据库实例，并加载 assets 中的预置数据库。

## 二、BusSchedule 与数据表映射
通过 Room 注解完成映射：使用 `@Entity` 指定对应数据表为 `Schedule`；`id` 设为主键；`stopName`、`arrivalTimeInMillis` 通过 `@ColumnInfo` 分别对应数据库 `stop_name`、`arrival_time` 字段，适配数据库下划线命名规则。

## 三、DAO 查询语句说明
两条查询分别用于**查询全部时刻表**和**根据站点名称查询对应班次**。统一按照 `arrival_time` 升序排序，是为了让到站时间由早到晚展示，符合公交时刻表的使用逻辑与用户阅读习惯。

## 四、createFromAsset 作用
该方法用于读取项目 `assets` 目录下的预置 SQLite 文件，应用首次运行时自动导入已有数据，无需手动插入数据，快速初始化数据库内容。

## 五、ViewModel 数据切换方式
修改 ViewModel，不再使用硬编码的示例数据，改为接收 DAO 对象。内部方法直接调用 DAO 的查询接口获取数据库数据，并通过 ViewModel 工厂创建数据库与 DAO 实例，完成数据来源切换。

## 六、Flow 在 Compose 中的使用
ViewModel 向外提供 `Flow` 类型数据，Compose 页面借助 `collectAsStateWithLifecycle` 收集数据流并转为界面可监听状态。数据变化时界面自动刷新，最终通过列表组件完成内容展示。

## 七、实验问题与解决
1. **编译报错**：缺失 Room KSP 编译器依赖，补充对应依赖并同步 Gradle 后解决。
2. **仍显示示例数据**：ViewModel 未完全移除旧代码、工厂类配置异常，核对并修改 ViewModel 逻辑与工厂配置即可。
3. **查询无数据**：表名、字段名拼写错误，修正 Entity 和 DAO 中的名称，保证与数据库一致。
4. **找不到预置数据库**：文件路径填写错误，修改 `createFromAsset` 内文件路径为正确相对路径。