# Lab14 使用 Room 完成 Bus Schedule 应用 实验报告

## 一、实验基本信息

- **实验名称**：使用 Room 完成公交时刻表（Bus Schedule）应用
- **实验环境**：Android Studio、Kotlin、Jetpack Compose、Room 2.7.0
- **实验目的**：掌握 Room 持久化库的使用方法，通过 Entity、DAO、Database 三层结构读取预置 SQLite 数据库中的数据，结合 ViewModel 与 Flow 向 Compose 界面展示真实的公交时刻表数据。

## 二、实验原理与知识点

Room 是 Android 官方推荐的 SQLite 持久化库，可显著简化原生 SQL 的开发工作。其核心包含三大组件：

1. **Entity（实体类）**：将 Kotlin 数据类映射为数据库表，通过注解定义表名、列名及主键。
2. **DAO（数据访问对象）**：定义数据库操作的接口，使用注解编写 SQL 语句，Room 在编译时自动生成实现代码。
3. **Database（数据库类）**：Room 数据库的主入口，继承 `RoomDatabase`，用于管理数据库实例、版本及实体集合，并统一对外提供 DAO 实例。

结合 Kotlin Flow 可实现数据的可观察性，配合 ViewModel 能够解耦数据层与 UI 层，保证界面在生命周期变化时的稳定性。最终在 Jetpack Compose 中收集并展示数据流，实现响应式 UI。

## 三、实验步骤与核心代码实现

### （一）配置 Room 依赖

**1. 项目级 `build.gradle.kts`（project）**
在全局配置中声明 Room 版本：

kotlin

```
extra.apply {
    set("room_version", "2.7.0")
}
```



**2. 模块级 `build.gradle.kts`（app）**
添加 Room 运行时库、Kotlin 扩展库以及 KSP 注解处理器：

kotlin

```
dependencies {
    implementation("androidx.room:room-runtime:${rootProject.extra["room_version"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["room_version"]}")
    ksp("androidx.room:room-compiler:${rootProject.extra["room_version"]}")
}
```



同步 Gradle，完成环境配置。

### （二）创建 Entity：`BusSchedule.kt`

将原有数据类改造为数据库实体，映射 `Schedule` 表：

kotlin

```
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Schedule")
data class BusSchedule(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "stop_name") val stopName: String,
    @ColumnInfo(name = "arrival_time") val arrivalTimeInMillis: Int
)
```



### （三）创建 DAO：`BusScheduleDao.kt`

定义数据查询接口，使用 `@Query` 编写 SQL，返回 `Flow` 实现数据监听：

kotlin

```
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BusScheduleDao {
    @Query("SELECT * FROM Schedule ORDER BY arrival_time ASC")
    fun getAll(): Flow<List<BusSchedule>>

    @Query("SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC")
    fun getByStopName(stopName: String): Flow<List<BusSchedule>>
}
```



### （四）创建 Database：`BusScheduleDatabase.kt`

定义数据库类，采用**单例模式**保证全局唯一实例，并通过 `createFromAsset` 加载预置数据库：

kotlin

```
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BusSchedule::class], version = 1, exportSchema = false)
abstract class BusScheduleDatabase : RoomDatabase() {
    abstract fun busScheduleDao(): BusScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: BusScheduleDatabase? = null

        fun getDatabase(context: Context): BusScheduleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    BusScheduleDatabase::class.java,
                    "bus_schedule_database"
                )
                .createFromAsset("database/bus_schedule.db")
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```



### （五）改造 ViewModel：`BusScheduleViewModel.kt`

移除原有的硬编码示例数据，对接 DAO，并通过自定义工厂提供 ViewModel 实例：

kotlin

```
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.busschedule.data.BusScheduleDao
import com.example.busschedule.data.BusScheduleDatabase
import kotlinx.coroutines.flow.Flow

class BusScheduleViewModel(
    private val busScheduleDao: BusScheduleDao
) : ViewModel() {
    fun getFullSchedule(): Flow<List<BusSchedule>> = busScheduleDao.getAll()
    fun getScheduleFor(stopName: String): Flow<List<BusSchedule>> =
        busScheduleDao.getByStopName(stopName)

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val db = BusScheduleDatabase.getDatabase(application)
                BusScheduleViewModel(db.busScheduleDao())
            }
        }
    }
}
```



### （六）运行验证

将应用部署至模拟器或真机，验证功能：

- 首页不再显示 `Example Street` 等测试数据，正常加载数据库内的 31 条真实记录；
- 数据按到站时间升序排列；
- 点击站点可跳转详情页，仅展示当前站点的所有到站记录；
- 返回导航、屏幕旋转、深浅色模式均工作正常。

## 四、核心问题解答

### 1. Entity、DAO、Database 三者的职责分别是什么？

- **Entity**：数据表映射模型，通过注解将 Kotlin 类与 SQLite 表（`Schedule`）绑定，定义表结构及字段映射，是数据的载体。
- **DAO**：数据访问接口，封装所有数据库操作，编写 SQL 语句，Room 在编译时自动生成实现，隔离数据访问逻辑。
- **Database**：数据库主入口，管理数据库版本、实体集合，并以单例模式提供全局唯一的数据库连接，对外暴露 DAO 实例。

### 2. `BusSchedule` 的属性与 `Schedule` 表是如何映射的？

预置数据库表名为 `Schedule`，映射关系如下：

| Kotlin 属性           | 数据库列名     | 注解                                 |
| :-------------------- | :------------- | :----------------------------------- |
| `id`                  | `id`           | `@PrimaryKey`                        |
| `stopName`            | `stop_name`    | `@ColumnInfo(name = "stop_name")`    |
| `arrivalTimeInMillis` | `arrival_time` | `@ColumnInfo(name = "arrival_time")` |

### 3. DAO 中的两条查询语句有何作用？为什么要排序？

- `SELECT * FROM Schedule ORDER BY arrival_time ASC`：查询所有公交时刻表记录，并按到站时间升序排列。
- `SELECT * FROM Schedule WHERE stop_name = :stopName ORDER BY arrival_time ASC`：根据站点名称筛选记录，同样按到站时间升序。

**排序原因**：公交时刻表需按车辆到站先后顺序展示，符合用户的查看习惯和业务逻辑。

### 4. `createFromAsset("database/bus_schedule.db")` 的作用是什么？

该方法用于加载预置数据库。当应用首次创建数据库时，Room 会直接读取 `assets/database/` 目录下的 `bus_schedule.db` 文件，将其中的表结构和数据完整复制到应用的本地数据库中，无需手动编写插入语句，即可快速完成数据初始化。

### 5. ViewModel 如何从示例数据切换为真实数据库数据？

1. 删除原代码中通过 `flowOf()` 创建的硬编码静态数据；
2. 修改 ViewModel 构造函数，接收 `BusScheduleDao` 作为数据源；
3. 将 `getFullSchedule()` 和 `getScheduleFor()` 方法的实现改为直接调用 DAO 的相应方法，返回数据库的 Flow；
4. 通过自定义 `viewModelFactory`，利用 Application 上下文创建 Room 数据库实例并获取 DAO，从而完成 ViewModel 的初始化。如此即彻底切换为数据库数据源。

### 6. `Flow<List<BusSchedule>>` 在 Compose 中是如何展示的？

- DAO 方法返回 `Flow`，Flow 是一种可观察的数据流，当数据库中的数据发生变化时会自动发射新数据；
- ViewModel 将 Flow 暴露给 Compose 界面，UI 层使用 `collectAsState()` 收集 Flow；
- `collectAsState()` 会将 Flow 转换为 Compose 可识别的 `State` 对象，每当数据流更新时，Compose 自动重组界面，实现实时刷新；
- 整个过程在后台线程执行，不会阻塞 UI 线程，保证应用流畅性。

### 7. 实验中遇到的问题及解决方法

| 问题                        | 原因                                                     | 解决方法                                                     |
| :-------------------------- | :------------------------------------------------------- | :----------------------------------------------------------- |
| 运行后仍显示示例数据        | ViewModel 中未完全删除 `flowOf` 测试代码                 | 清空硬编码数据，确保方法直接调用 DAO 接口                    |
| 应用启动崩溃，提示表不存在  | `@Entity(tableName = "")` 中的表名与数据库实际表名不一致 | 修正为 `@Entity(tableName = "Schedule")`                     |
| Room 编译报错，注解无法识别 | 未添加 KSP 编译器依赖                                    | 在模块级依赖中添加 `ksp("androidx.room:room-compiler:2.7.0")` 并同步 |
| 查询单个站点时返回数据为空  | SQL 语句中的字段名 `stop_name` 拼写错误                  | 核对数据库列名，确保 SQL 语句与原表字段完全一致              |

## 五、实验总结

本次实验完整实现了基于 Room 的数据层开发，成功将预置 SQLite 数据库中的数据通过三层架构（Entity、DAO、Database）读取，并最终展示在 Compose 界面上。通过实践，掌握了 Room 三大核心组件的使用方法与设计理念，理解了 Entity 的表映射、DAO 的查询编写、Database 的单例管理等关键知识。

同时，结合 Kotlin Flow 实现了响应式的数据更新机制，配合 ViewModel 完成了 UI 层与数据层的解耦，确保了界面生命周期安全。在实验过程中，排查并解决了依赖配置、表名与字段匹配、Flow 数据收集等常见问题，加深了对 Android 本地持久化以及 Jetpack 组件协同工作的理解，圆满达成了实验目标。