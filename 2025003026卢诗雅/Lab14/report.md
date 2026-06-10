# Lab14：使用 Room 完成 Bus Schedule 应用

## 1. Entity、DAO、Database 三者在本实验中的职责说明

### Entity（实体）

在本实验中，`BusSchedule` 被标注为 Room 的 `@Entity`，其核心职责是完成 Kotlin 数据类与 SQLite 数据库中 `Schedule` 表的映射。它定义了表的结构：通过注解指定表名、主键、列名，将内存中的 `BusSchedule` 对象与数据库中的行数据一一对应，是 Room 操作数据库的 “数据模型载体”，确保代码中的数据结构与底层 SQLite 表结构一致。

### DAO（数据访问对象）

`BusScheduleDao` 作为 DAO 接口，是数据库操作的 “统一入口”。其职责是封装所有对 `Schedule` 表的查询逻辑，本实验中定义了 `getAll()` 和 `getByStopName()` 两个方法，分别用于获取完整时刻表和指定站点的时刻表。Room 会根据接口中的 `@Query` 注解自动生成 SQL 执行的实现代码，隔离了底层 SQL 操作与上层业务逻辑，让 ViewModel 无需直接编写 SQL，只需调用 DAO 方法即可完成数据读写。

### Database（数据库）

`BusScheduleDatabase` 是继承自 `RoomDatabase` 的抽象类，是整个 Room 数据库的 “核心管理类”。其核心职责包括：

- 声明数据库关联的 Entity（`BusSchedule`），指定数据库版本；

- 提供 DAO 接口的实例，让上层代码能获取 DAO 对象；

- 通过单例模式创建数据库实例，避免重复初始化；

- 配置从 assets 目录加载预置数据库，完成数据库的初始化。

## 2. BusSchedule 属性如何映射到 Schedule 表和列

本实验中通过 Room 注解完成 `BusSchedule` 属性与 `Schedule` 表列的精准映射，映射关系如下：

|Kotlin 属性|数据库列名|注解与映射说明|
|---|---|---|
|`id: Int`|`id`|标注 `@PrimaryKey`，指定该属性为 `Schedule` 表的主键，列名默认与属性名一致（无需额外指定）|
|`stopName: String`|`stop_name`|标注 `@ColumnInfo(name = "stop_name")`，显式映射到表中的 `stop_name` 列|
|`arrivalTimeInMillis: Int`|`arrival_time`|标注 `@ColumnInfo(name = "arrival_time")`，显式映射到表中的 `arrival_time` 列|

核心代码示例：

```kotlin
@Entity(tableName = "Schedule")
data class BusSchedule(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "stop_name") val stopName: String,
    @ColumnInfo(name = "arrival_time") val arrivalTimeInMillis: Int
)
```

其中 `@Entity(tableName = "Schedule")` 确保 `BusSchedule` 对应数据库中的 `Schedule` 表（而非默认的类名 BusSchedule），是映射的关键前提。

## 3. 两条 DAO 查询语句的作用，以及为什么需要按 arrival_time 排序

### 两条查询语句的作用

1. `@Query("SELECT * FROM Schedule ORDER BY arrival_time ASC") fun getAll(): Flow<List<BusSchedule>>`：
作用是查询 `Schedule` 表中所有记录，返回完整的公交时刻表数据，供应用首页展示全部站点的到站信息。

2. `@Query("SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC") fun getByStopName(stopName: String): Flow<List<BusSchedule>>`：
作用是根据传入的 `stopName`（站点名称）过滤数据，只返回该站点的所有到站记录，供站点详情页展示。

### 按 arrival_time 排序的原因

- **业务合理性**：公交时刻表的核心需求是按 “到站时间从早到晚” 展示，`arrival_time` 存储的是 Unix 时间戳，按其升序（ASC）排序后，数据会按时间先后排列，符合用户查看时刻表的习惯；

- **用户体验**：若不排序，数据会按数据库存储的主键（id）无序展示，用户无法直观看到站点的到站顺序，降低使用体验；

- **数据一致性**：无论是完整时刻表还是单个站点的时刻表，统一按时间排序能让整个应用的时间展示逻辑保持一致。

## 4. createFromAsset ("database/bus_schedule.db") 的作用

`createFromAsset("database/bus_schedule.db")` 是 Room 提供的数据库初始化方法，在本实验中的核心作用是：

1. **加载预置数据**：将项目 `assets/database/` 目录下的 `bus_schedule.db`（预置了 31 条公交时刻表数据的 SQLite 数据库文件）复制到应用的私有存储目录中，作为 Room 数据库的初始数据；

2. **避免空数据**：若不使用该方法，Room 会创建一个空的 `Schedule` 表，应用启动后无数据展示；通过该方法，应用首次启动时即可加载真实的公交数据，无需手动插入；

3. **简化开发**：无需编写批量插入数据的代码，直接复用预置的 SQLite 数据库，快速完成数据层的初始化。

## 5. ViewModel 如何从示例数据切换为数据库数据

起始代码中，`BusScheduleViewModel` 通过 `flowOf()` 返回写死的示例数据，切换为数据库数据的核心步骤如下：

### 步骤 1：修改 ViewModel 构造函数

新增 `BusScheduleDao` 作为构造参数，让 ViewModel 能通过 DAO 访问数据库：

```kotlin
class BusScheduleViewModel(
    private val busScheduleDao: BusScheduleDao
) : ViewModel() { ... }
```

### 步骤 2：替换数据返回逻辑

删除 `flowOf()` 包裹的示例数据，改为调用 DAO 的方法返回数据库数据：

```kotlin
// 原示例代码（删除）
fun getFullSchedule(): Flow<List<BusSchedule>> = flowOf(listOf(BusSchedule(0, "Example Street", 0)))

// 新逻辑（调用 DAO）
fun getFullSchedule(): Flow<List<BusSchedule>> = busScheduleDao.getAll()
fun getScheduleFor(stopName: String): Flow<List<BusSchedule>> = busScheduleDao.getByStopName(stopName)
```

### 步骤 3：配置 ViewModel 工厂

通过 `APPLICATION_KEY` 获取 Application 上下文，创建 `BusScheduleDatabase` 实例并获取 DAO，确保 ViewModel 初始化时能拿到有效的 DAO 对象：

```kotlin
companion object {
    val factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val application = checkNotNull(this[APPLICATION_KEY])
            val database = BusScheduleDatabase.getDatabase(application)
            BusScheduleViewModel(database.busScheduleDao())
        }
    }
}
```

### 核心逻辑

通过 “依赖注入 DAO + 替换数据来源”，ViewModel 从 “返回硬编码示例数据” 切换为 “通过 DAO 从 Room 数据库读取真实数据”，且保持返回值类型（`Flow<List<BusSchedule>>`）不变，无需修改 Compose UI 层的代码。

## 6. Flow\<List\> 如何被 Compose 页面收集并显示

Compose 页面通过 Jetpack Compose 与 Coroutine、Flow 的协同机制收集 `Flow` 数据并展示，核心流程如下：

### 步骤 1：在 ViewModel 中暴露 Flow 数据

ViewModel 的 `getFullSchedule()` 和 `getScheduleFor()` 方法返回 `Flow<List<BusSchedule>>`，该 Flow 由 Room 自动生成，会在数据库数据变化时自动发送新数据。

### 步骤 2：在 Composable 中收集 Flow

使用 `collectAsStateWithLifecycle()` 方法（生命周期感知的收集方式）将 Flow 转换为 Compose 可观察的 State 对象，确保在组件处于活跃状态时才收集数据，避免内存泄漏：

```kotlin
@Composable
fun FullScheduleScreen(viewModel: BusScheduleViewModel = viewModel(factory = BusScheduleViewModel.factory)) {
    // 收集完整时刻表 Flow，转换为 State
    val scheduleItems by viewModel.getFullSchedule().collectAsStateWithLifecycle(initialValue = emptyList())
    
    // 展示数据
    LazyColumn {
        items(scheduleItems) { schedule ->
            ScheduleItem(schedule = schedule)
        }
    }
}

@Composable
fun StopScheduleScreen(stopName: String, viewModel: BusScheduleViewModel = viewModel(factory = BusScheduleViewModel.factory)) {
    // 收集指定站点的时刻表 Flow
    val stopSchedule by viewModel.getScheduleFor(stopName).collectAsStateWithLifecycle(initialValue = emptyList())
    
    LazyColumn {
        items(stopSchedule) { schedule ->
            ScheduleItem(schedule = schedule)
        }
    }
}
```

### 步骤 3：自动刷新 UI

当 Flow 发送新数据（如数据库数据变化）时，`collectAsStateWithLifecycle()` 会自动更新 State 对象，Compose 会感知到 State 变化并重组 UI，从而展示最新的时刻表数据。

## 7. 实验中遇到的问题与解决过程

### 问题 1：应用启动后仍显示示例数据

#### 现象

添加 Room 相关代码后，应用运行仍只显示 “Example Street \| 12:00 AM”，未加载数据库数据。

#### 原因

ViewModel 工厂配置错误，未正确传入 DAO，导致 ViewModel 仍使用旧的示例数据逻辑。

#### 解决过程

1. 检查 `BusScheduleViewModel` 的 `factory` 代码，发现遗漏了 `APPLICATION_KEY` 的导入；

2. 补全导入 `import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY`；

3. 确认 `BusScheduleDatabase.getDatabase(application)` 正确创建数据库实例，并将 DAO 传入 ViewModel；

4. 重新运行应用，数据正常加载。

### 问题 2：Room 编译报错 “Cannot find symbol BusScheduleDatabase_Impl”

#### 现象

添加 `BusScheduleDatabase` 后，编译提示找不到自动生成的 `BusScheduleDatabase_Impl` 类。

#### 原因

未正确添加 Room KSP 编译器依赖，导致 Room 无法生成数据库实现类。

#### 解决过程

1. 检查 `app/build.gradle.kts`，发现只添加了 `room-ktx` 和 `room-runtime`，遗漏了 `ksp("androidx.room:room-compiler:...")`；

2. 补全依赖：

    ```kotlin
    ksp("androidx.room:room-compiler:${rootProject.extra["room_version"]}")
    ```

3. 同步 Gradle 并重新编译，报错消失。

### 问题 3：查询结果为空，无任何数据展示

#### 现象

应用无报错，但列表为空，未显示任何公交站点数据。

#### 原因

`@Entity(tableName = "Schedule")` 被误写为 `tableName = "BusSchedule"`，与预置数据库的表名不一致，Room 查询不到数据。

#### 解决过程

1. 打开预置数据库 `bus_schedule.db`，确认表名是 `Schedule`；

2. 修改 `BusSchedule` 的 `@Entity` 注解，将表名改为 `Schedule`；

3. 清理应用数据（避免旧数据库缓存），重新运行，数据正常显示。

### 问题 4：Compose 页面提示 “Flow 收集在主线程阻塞”

#### 现象

运行应用时 Logcat 提示 “Inappropriate blocking method call”，Flow 收集可能阻塞主线程。

#### 原因

初始尝试使用 `collectAsState()` 收集 Flow，未使用生命周期感知的方法，且 Room 的 Flow 虽在后台线程发送数据，但收集方式未适配 Compose 生命周期。

#### 解决过程

1. 将 `collectAsState()` 替换为 `collectAsStateWithLifecycle()`（需导入 `androidx.lifecycle.compose:lifecycle-compose` 依赖）；

2. 该方法会在组件进入非活跃状态时自动暂停收集，避免主线程阻塞，同时保证数据仅在组件活跃时更新。
