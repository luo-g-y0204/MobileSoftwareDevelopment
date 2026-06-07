# Lab13：创建 Bookshelf 网络书架应用

## 一、实验目的

本实验基于 MVVM（Model-View-ViewModel）架构，结合 Retrofit 网络通信框架、Repository 数据仓库模式以及 Jetpack Compose 声明式 UI 技术，实现一个能够从网络获取并展示图书图片列表的 Bookshelf 应用。

通过本实验，掌握以下核心知识点：

1. 使用 Retrofit 实现网络请求与 JSON 数据解析；
2. 利用 Gson 完成网络数据与 Kotlin 对象之间的转换；
3. 采用 Repository 模式实现数据层与业务层解耦；
4. 使用 ViewModel 管理界面状态及业务逻辑；
5. 利用 StateFlow 实现响应式数据更新；
6. 使用 Jetpack Compose 构建现代化 Android UI；
7. 基于 Apifox Mock 接口完成前后端分离开发流程。

------

# 二、实验内容与实现过程

## 1. 使用 Apifox Mock 接口作为数据源

在实际开发过程中，前端开发往往受制于后端接口的开发进度。为了保证实验能够独立完成，本实验采用 Apifox Mock 接口模拟真实服务端数据。

### Apifox Mock 的优势

#### （1）保证接口稳定性

Mock 接口能够持续返回预设数据，不受后端开发进度和服务器状态影响，确保实验过程顺利进行。

#### （2）支持前后端并行开发

开发人员可根据约定好的接口文档提前完成数据请求与界面开发，提高开发效率。

#### （3）便于功能测试

可以灵活修改返回数据，模拟各种业务场景，例如：

- 正常数据返回；
- 空数据返回；
- 异常数据返回；
- 网络错误场景。

从而验证应用的健壮性与容错能力。

#### （4）降低后期适配成本

Mock 数据结构与真实接口保持一致，后续接入正式后端时仅需修改接口地址即可完成迁移。

本实验使用的 Mock 接口地址如下：

```text
https://m1.apifoxmock.com/m1/8321477-8085280-default/photos
```

返回的数据格式示例：

```json
[
  {
    "id":"1",
    "img_src":"https://..."
  }
]
```

------

## 2. Retrofit 网络服务接口设计

Retrofit 是 Android 中常用的网络请求框架，其核心思想是通过接口定义 HTTP 请求。

### （1）添加依赖

在 `build.gradle.kts` 中添加相关依赖：

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.google.code.gson:gson:2.10.1")
implementation("io.coil-kt:coil-compose:2.7.0")
```

其中：

| 依赖           | 作用       |
| -------------- | ---------- |
| Retrofit       | 网络请求   |
| Gson Converter | JSON解析   |
| Gson           | 对象序列化 |
| Coil           | 图片加载   |

------

### （2）定义 API 接口

```kotlin
interface BookshelfApiService {

    @GET("photos")
    suspend fun getBooks(): List<BookDto>
}
```

说明：

- `@GET("photos")` 指定请求方式和路径；
- `suspend` 支持 Kotlin 协程；
- Retrofit 自动完成网络请求；
- Gson 自动完成 JSON 转对象操作。

------

### （3）创建 Retrofit 单例

```kotlin
object RetrofitClient {

    private const val BASE_URL =
        "https://m1.apifoxmock.com/m1/8321477-8085280-default/"

    val api: BookshelfApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(BookshelfApiService::class.java)
    }
}
```

采用单例模式管理 Retrofit 对象，避免重复创建，提高资源利用率。

------

## 3. Repository 模式实现数据源隔离

Repository 位于 ViewModel 与数据源之间，负责统一管理数据获取逻辑。

其作用如下：

- 隐藏数据来源；
- 降低模块耦合；
- 提高代码可维护性；
- 方便后期扩展数据库、本地缓存等功能。

### （1）定义 Repository 接口

```kotlin
interface BooksRepository {

    suspend fun getBooks(): List<Book>

    suspend fun getBook(id: String): Book
}
```

ViewModel 只依赖该接口，不直接访问网络层。

------

### （2）网络数据源实现

```kotlin
class NetworkBooksRepository(
    private val api: BookshelfApiService
) : BooksRepository {

    override suspend fun getBooks(): List<Book> {
        return api.getBooks()
            .map { it.asExternalModel() }
    }

    override suspend fun getBook(id: String): Book {
        return getBooks()
            .first { it.id == id }
    }
}
```

主要职责：

1. 调用 Retrofit 获取网络数据；
2. DTO 转换为业务模型；
3. 向上层提供统一数据接口。

------

### （3）离线数据源实现

```kotlin
class OfflineBooksRepository : BooksRepository {

    private val localBooks = listOf(
        Book("off1","Offline Book 1","https://picsum.photos/id/10/800/600"),
        Book("off2","Offline Book 2","https://picsum.photos/id/11/800/600"),
        Book("off3","Offline Book 3","https://picsum.photos/id/12/800/600")
    )

    override suspend fun getBooks() = localBooks

    override suspend fun getBook(id: String) =
        localBooks.first { it.id == id }
}
```

该实现用于：

- 网络不可用；
- 接口异常；
- 本地测试。

------

### （4）统一管理 Repository

```kotlin
class AppContainer {

    private val apiService =
        RetrofitClient.api

    val networkBooksRepository =
        NetworkBooksRepository(apiService)

    val offlineBooksRepository =
        OfflineBooksRepository()

    val defaultBooksRepository =
        networkBooksRepository
}
```

通过依赖注入统一管理数据源，实现灵活切换。

------

## 4. UI 状态管理与页面刷新机制

### （1）定义界面状态

采用密封类统一描述界面状态：

```kotlin
sealed interface BookshelfUiState {

    data object Loading : BookshelfUiState

    data class Success(
        val books: List<Book>
    ) : BookshelfUiState

    data class Error(
        val msg: String
    ) : BookshelfUiState
}
```

状态划分如下：

| 状态    | 作用         |
| ------- | ------------ |
| Loading | 加载中       |
| Success | 数据获取成功 |
| Error   | 数据获取失败 |

------

### （2）ViewModel 状态管理

```kotlin
class BookshelfViewModel(
    private val repo: BooksRepository
) : ViewModel() {

    private val _ui =
        MutableStateFlow<BookshelfUiState>(
            BookshelfUiState.Loading
        )

    val ui = _ui.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {

        _ui.value =
            BookshelfUiState.Loading

        viewModelScope.launch {

            try {

                val books =
                    repo.getBooks()

                _ui.value =
                    BookshelfUiState.Success(books)

            } catch (e: Exception) {

                _ui.value =
                    BookshelfUiState.Error(
                        e.message ?: "Unknown Error"
                    )
            }
        }
    }
}
```

实现流程：

1. 设置 Loading 状态；
2. 调用 Repository 获取数据；
3. 成功切换 Success；
4. 异常切换 Error；
5. 用户重试后重新发起请求。

------

### （3）Compose 响应式渲染

```kotlin
@Composable
fun BookshelfScreen(
    vm: BookshelfViewModel
) {

    val uiState =
        vm.ui.collectAsState()

    when (val state = uiState.value) {

        is BookshelfUiState.Loading -> {
            CircularProgressIndicator()
        }

        is BookshelfUiState.Success -> {
            BookGrid(state.books)
        }

        is BookshelfUiState.Error -> {
            ErrorPage(
                state.msg,
                vm::loadBooks
            )
        }
    }
}
```

Compose 会自动监听 StateFlow 数据变化，并重新组合界面，实现响应式更新。

------

# 三、运行结果分析

## 加载中（Loading）

应用启动后立即显示圆形进度条，提示用户正在加载网络数据。

## 加载成功（Success）

成功获取数据后，以网格布局展示图书封面图片，界面流畅且支持滚动浏览。

## 加载失败（Error）

当网络异常或接口访问失败时，界面显示错误提示信息，并提供“重新加载”按钮供用户再次尝试。

------

# 四、实验过程中遇到的问题及解决方案

### 问题一：collectAsStateWithLifecycle 无法识别

**原因：**

缺少 Lifecycle Compose 相关依赖或版本不匹配。

**解决方法：**

使用 Compose 自带的：

```kotlin
collectAsState()
```

替代：

```kotlin
collectAsStateWithLifecycle()
```

成功解决兼容性问题。

------

### 问题二：JSON 数据解析失败

**原因：**

接口字段名称与 DTO 属性名称不一致。

例如：

```json
img_src
```

对应：

```kotlin
imgSrc
```

**解决方法：**

```kotlin
@SerializedName("img_src")
val imgSrc: String
```

建立字段映射关系。

------

### 问题三：网络请求导致界面卡顿

**原因：**

在主线程执行耗时网络操作。

**解决方法：**

使用：

```kotlin
suspend
```

与：

```kotlin
viewModelScope.launch
```

实现协程异步请求。

------

### 问题四：图片无法显示

**原因：**

缺少网络权限或图片加载库依赖。

**解决方法：**

添加网络权限：

```xml
<uses-permission
    android:name="android.permission.INTERNET"/>
```

并引入 Coil：

```kotlin
implementation("io.coil-kt:coil-compose:2.7.0")
```

成功解决图片加载问题。

------

# 五、实验总结

通过本次 Bookshelf 网络书架应用开发实验，我系统学习了 Android 现代化应用开发流程，深入理解了 MVVM 架构的设计思想及各层职责划分。在实践过程中，掌握了 Retrofit 网络请求、Gson 数据解析、Repository 数据仓库模式以及 StateFlow 状态管理等关键技术，并能够结合 Jetpack Compose 实现响应式界面开发。

同时，通过使用 Apifox Mock 接口完成前后端分离开发，进一步理解了实际项目中的协同开发模式。实验过程中还解决了数据解析、状态管理、协程异步请求以及图片加载等问题，提升了独立分析和解决问题的能力。

总体而言，本实验不仅巩固了 Android 网络开发基础，也加深了对 MVVM 架构、响应式编程思想和现代 Android 开发技术体系的理解，为后续开发更复杂的移动应用奠定了良好的基础。
