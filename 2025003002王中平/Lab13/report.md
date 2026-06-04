# Lab13：创建 Bookshelf 网络书架应用

## 一、实验目的

本实验基于 MVVM 架构，结合 Retrofit 网络请求、Repository 数据层模式和 Jetpack Compose，实现一个从网络加载并展示图书图片列表的应用。通过本实验，掌握以下核心技术：

1. 基于 Retrofit 实现网络请求与 Gson 数据解析

2. 使用 Repository 模式隔离网络数据源

3. 利用 ViewModel 管理 UI 状态（Loading/Success/Error）

4. 使用 Apifox Mock 接口作为数据源，完成数据驱动的 UI 开发

---

## 二、实验内容与步骤

### 1. 为什么本实验改用 Apifox Mock 接口

在传统网络请求开发中，后端接口未完成、不稳定或无法访问，会导致前端开发停滞。本实验改用 Apifox Mock 接口，核心优势如下：

- **接口稳定可用**：Apifox 提供的 Mock 接口无需依赖真实后端，始终稳定返回预设的 JSON 数据，避免了后端未完成、网络波动等问题，保证实验开发不受影响。

- **数据结构可自定义**：可提前约定并模拟接口的请求方式、字段结构和返回格式，与后续真实后端接口完全兼容，降低了开发后期的适配成本。

- **开发流程解耦**：前端开发无需等待后端接口开发完成，可基于 Mock 接口独立完成网络请求、数据解析和 UI 展示的全流程开发，实现前后端并行开发。

- **便于调试测试**：可随时修改 Mock 接口的返回数据（如模拟空数据、异常数据），方便测试应用的异常处理逻辑，提升应用的健壮性。

本实验使用的 Apifox Mock 接口地址：`https://m1\.apifoxmock\.com/m1/8321477\-8085280\-default/photos`，接口返回 JSON 格式的图书图片数据，包含 `id` 和 `img\_src` 字段，可直接用于 Retrofit 解析。

---

### 2. Retrofit 服务接口如何定义

Retrofit 是一个基于 OkHttp 的类型安全的 RESTful 网络请求框架，本实验通过以下步骤完成服务接口定义：

#### （1）添加依赖

在 `app/build\.gradle\.kts` 中添加 Retrofit 核心依赖、Gson 转换器和 Coil 图片加载依赖：

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.google.code.gson:gson:2.10.1")
implementation("io.coil-kt:coil-compose:2.7.0")
```

#### （2）定义 API 服务接口 `BookshelfApiService`

使用 Retrofit 注解定义请求方式和路径，声明挂起函数适配协程：

```kotlin
interface BookshelfApiService {
    @GET("photos")
    suspend fun getBooks(): List<BookDto>
}
```

- `@GET\(\&\#34;photos\&\#34;\)`：指定请求方式为 GET，请求路径为 `photos`，拼接基础地址后形成完整请求 URL。

- `suspend fun getBooks\(\)`：使用 `suspend` 关键字，让请求可在协程中执行，避免阻塞主线程。

- 返回值 `List\&lt;BookDto\&gt;`：接口返回的 JSON 数据将被 Gson 自动解析为 `BookDto` 数据传输对象列表。

#### （3）创建 Retrofit 实例

通过 Retrofit.Builder 配置基础地址、Gson 转换器，创建服务实例：

```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://m1.apifoxmock.com/m1/8321477-8085280-default/"

    val api: BookshelfApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookshelfApiService::class.java)
    }
}
```

- `baseUrl`：配置 Apifox Mock 接口的基础地址。

- `addConverterFactory\(GsonConverterFactory\.create\(\)\)`：添加 Gson 转换器，用于 JSON 数据的序列化和反序列化。

- `create\(BookshelfApiService::class\.java\)`：动态生成 `BookshelfApiService` 的实现类，用于发起网络请求。

---

### 3. Repository 如何隔离网络数据源

Repository 是 MVVM 架构中数据层的核心组件，作用是隔离数据源，为上层 ViewModel 提供统一的数据访问接口，本实验通过以下方式实现数据源隔离：

#### （1）定义 Repository 接口

定义 `BooksRepository` 接口，声明数据访问方法，屏蔽底层数据源差异：

```kotlin
interface BooksRepository {
    suspend fun getBooks(): List<Book>
    suspend fun getBook(id: String): Book
}
```

#### （2）实现网络数据源 `NetworkBooksRepository`

实现 `BooksRepository` 接口，封装网络请求逻辑，调用 Retrofit 服务获取数据：

```kotlin
class NetworkBooksRepository(
    private val api: BookshelfApiService
) : BooksRepository {
    override suspend fun getBooks(): List<Book> {
        // 调用 Retrofit 接口获取 DTO，转换为领域模型 Book
        return api.getBooks().map { it.asExternalModel() }
    }

    override suspend fun getBook(id: String): Book {
        return getBooks().first { it.id == id }
    }
}
```

#### （3）实现离线兜底数据源 `OfflineBooksRepository`

同样实现 `BooksRepository` 接口，提供本地模拟数据，作为网络请求失败时的兜底方案：

```kotlin
class OfflineBooksRepository : BooksRepository {
    private val localBooks = listOf(
        Book("off1", "Offline Book 1", "https://picsum.photos/id/10/800/600"),
        Book("off2", "Offline Book 2", "https://picsum.photos/id/11/800/600"),
        Book("off3", "Offline Book 3", "https://picsum.photos/id/12/800")
    )

    override suspend fun getBooks(): List<Book> = localBooks
    override suspend fun getBook(id: String): Book = localBooks.first { it.id == id }
}
```

#### （4）依赖注入容器 `AppContainer`

通过 `AppContainer` 集中管理 Repository 实例，实现数据源的统一配置：

```kotlin
class AppContainer {
    private val apiService: BookshelfApiService = RetrofitClient.api
    val networkBooksRepository: BooksRepository = NetworkBooksRepository(apiService)
    val offlineBooksRepository: BooksRepository = OfflineBooksRepository()
    val defaultBooksRepository: BooksRepository = networkBooksRepository
}
```

#### 隔离逻辑说明

- 上层 ViewModel 仅依赖 `BooksRepository` 接口，无需关心底层是网络数据源还是离线数据源。

- 如需切换数据源（如网络切换为本地数据库），仅需新增 `BooksRepository` 实现类，无需修改 ViewModel 代码，实现了数据层与 UI 层的解耦。

- 网络请求失败时，可自动切换到 `OfflineBooksRepository`，提升应用的稳定性。

---

### 4. 应用的 Loading / Success / Error 状态如何切换

本实验通过 ViewModel 管理 UI 状态，使用密封类 `BookshelfUiState` 定义三种状态，并通过 `StateFlow` 实现状态的响应式切换。

#### （1）定义 UI 状态密封类

```kotlin
sealed interface BookshelfUiState {
    data object Loading : BookshelfUiState
    data class Success(val books: List<Book>) : BookshelfUiState
    data class Error(val msg: String) : BookshelfUiState
}
```

- `Loading`：数据加载中状态，显示进度指示器。

- `Success`：数据加载成功状态，包含图书列表数据。

- `Error`：数据加载失败状态，包含错误信息。

#### （2）ViewModel 中状态切换逻辑

在 `BookshelfViewModel` 中，通过 `MutableStateFlow` 管理状态，在协程中执行网络请求并更新状态：

```kotlin
class BookshelfViewModel(
    private val repo: BooksRepository
) : ViewModel() {
    private val _ui = MutableStateFlow<BookshelfUiState>(BookshelfUiState.Loading)
    val ui: StateFlow<BookshelfUiState> = _ui.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        // 切换为 Loading 状态
        _ui.value = BookshelfUiState.Loading
        viewModelScope.launch {
            try {
                // 调用 Repository 获取数据
                val books = repo.getBooks()
                // 请求成功，切换为 Success 状态
                _ui.value = BookshelfUiState.Success(books)
            } catch (e: Exception) {
                // 请求失败，切换为 Error 状态
                _ui.value = BookshelfUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

#### （3）Compose 中响应状态变化

在 `BookshelfScreen` 中，通过 `collectAsState` 收集状态，根据不同状态渲染对应的 UI：

```kotlin
@Composable
fun BookshelfScreen(vm: BookshelfViewModel) {
    val uiState = vm.ui.collectAsState()

    Scaffold(...) {
        when (val state = uiState.value) {
            is BookshelfUiState.Loading -> CircularProgressIndicator()
            is BookshelfUiState.Success -> BookGrid(state.books)
            is BookshelfUiState.Error -> ErrorPage(state.msg, vm::loadBooks)
        }
    }
}
```

#### 状态切换流程

1. 应用启动时，ViewModel 初始化，自动调用 `loadBooks\(\)`，UI 切换为 `Loading` 状态，显示进度条。

2. 网络请求成功，Repository 返回数据，ViewModel 将状态更新为 `Success`，Compose 渲染图书列表。

3. 网络请求失败（如无网络、接口异常），捕获异常后状态更新为 `Error`，显示错误信息和重试按钮。

4. 用户点击重试按钮，再次调用 `loadBooks\(\)`，状态重新切换为 `Loading`，重复上述流程。

---

## 三、运行截图

1. **加载中状态**：启动应用后，显示圆形进度指示器。

2. **加载成功状态**：图书图片以网格形式展示，点击图片可弹出详情弹窗。

3. **加载失败状态**：显示错误提示信息，底部包含 “重试” 按钮，点击可重新发起请求。

---

## 四、遇到的问题与解决方案

1. **问题 1：****`collectAsStateWithLifecycle`**** 无法解析**

    - 原因：缺少 `androidx\.lifecycle:lifecycle\-viewmodel\-compose` 依赖，或依赖版本不兼容。

    - 解决方案：将 `collectAsStateWithLifecycle` 替换为基础的 `collectAsState`，简化状态收集逻辑，无需额外依赖即可正常使用。

2. **问题 2：Retrofit 接口返回数据无法解析**

    - 原因：JSON 字段名与 DTO 类字段名不匹配（如接口返回 `img\_src`，类中定义为 `imgSrc`）。

    - 解决方案：在 `BookDto` 类中使用 `@SerializedName\(\&\#34;img\_src\&\#34;\)` 注解，指定 JSON 字段与类字段的映射关系，确保 Gson 能正确解析数据。

3. **问题 3：网络请求被主线程阻塞**

    - 原因：未使用协程执行网络请求，直接在主线程中调用 Retrofit 接口，导致应用卡顿或崩溃。

    - 解决方案：在 Retrofit 服务接口中使用 `suspend` 关键字声明请求方法，在 ViewModel 的 `viewModelScope` 中调用，确保请求在后台线程执行，不阻塞主线程。

4. **问题 4：图片加载失败**

    - 原因：未添加网络权限，或 Coil 图片加载依赖缺失。

    - 解决方案：在 `AndroidManifest\.xml` 中添加 `\&lt;uses\-permission android:name=\&\#34;android\.permission\.INTERNET\&\#34; /\&gt;`，并确保 `build\.gradle\.kts` 中已添加 Coil 依赖。

---

## 五、实验总结

本实验基于 Apifox Mock 接口，结合 Retrofit、Repository 模式和 Jetpack Compose，完成了一个图书列表应用的开发。通过本实验，掌握了 Retrofit 网络请求的定义与实现、Repository 数据源隔离的设计思想，以及 ViewModel 管理 UI 状态的方法，理解了 MVVM 架构中各组件的职责与协作流程。同时，通过处理网络异常、UI 状态切换等问题，提升了 Android 应用开发的问题排查与调试能力。
