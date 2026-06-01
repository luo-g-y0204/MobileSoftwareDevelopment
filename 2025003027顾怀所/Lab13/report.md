# Lab13：创建 Bookshelf 网络书架应用 实验报告

## 一、实验概述
本次实验要求使用 Kotlin + Jetpack Compose 完成一个网络图片书架应用。
核心任务包括：网络请求、JSON 解析、图片加载、状态管理、Repository 数据层、依赖注入、UI 状态切换等。
实验使用 Apifox Mock 接口作为稳定数据源，综合练习协程、Retrofit、Gson、Coil 等 Android 开发核心技术。

## 二、实验目的
1. 掌握 Retrofit + Gson 进行网络请求与 JSON 数据解析。
2. 学会使用 Repository 模式隔离数据层，实现网络与本地数据解耦。
3. 掌握依赖注入的基本思想，使用 AppContainer 统一管理对象实例。
4. 学会使用 Coil 在 Compose 中加载网络图片。
5. 掌握 UI 状态管理：Loading / Success / Error 三种状态切换。
6. 掌握 LazyVerticalGrid 实现图片网格列表布局。
7. 学会处理网络异常、加载失败、重试逻辑、断网兜底等工程化能力。
8. 理解 Android 官方推荐的应用架构：UI → ViewModel → Repository → Data Source。

## 三、实验环境
- 开发工具：Android Studio
- 编程语言：Kotlin
- 架构：MVVM
- 网络框架：Retrofit 2.11.0
- 解析库：Gson
- 图片加载：Coil 2.7.0
- UI 框架：Jetpack Compose
- 数据源：Apifox Mock 在线接口

## 四、实验原理与关键知识点

### 1. 为什么使用 Apifox Mock 接口
本次实验改用 Apifox Mock 接口主要有以下原因：
1. 接口稳定，不会因后端服务变动导致实验无法进行。
2. 返回格式固定，便于解析与调试。
3. 使用 HTTPS 协议，不需要配置明文流量许可。
4. 全球可访问，适合教学实验。
5. 数据可控，返回固定图片列表，便于展示网格布局。

### 2. Retrofit 服务接口定义
Retrofit 是类型安全的网络请求库，通过注解描述 HTTP 请求。
本次实验定义的接口：

interface BookshelfApiService {
    @GET("photos")
    suspend fun getBooks(): List<BookDto>
}

- @GET(“photos”) 表示发送 GET 请求到 /photos 路径。
- suspend 表示挂起函数，必须在协程中调用。
- 返回值为 List<BookDto>，Gson 会自动将 JSON 数组转为对象列表。

### 3. Repository 数据层模式
Repository 的作用：
1. 隔离数据来源：ViewModel 不关心数据来自网络还是本地。
2. 统一数据入口：便于统一处理异常、日志、缓存。
3. 降低耦合：便于替换数据源、进行单元测试。
4. 提高可维护性：网络逻辑与 UI 完全分离。

本次实验实现两种 Repository：
- NetworkBooksRepository：从网络获取真实数据。
- OfflineBooksRepository：网络异常时提供兜底数据。

### 4. 依赖注入与 AppContainer
AppContainer 负责统一创建：
- Retrofit 实例
- ApiService 实例
- Repository 实例

好处：
- 避免重复创建网络对象。
- 便于全局替换数据源。
- 符合单一职责原则。
- 方便测试。

### 5. UI 状态管理
应用定义三种状态：
- Loading：加载中，显示进度条。
- Success：加载成功，显示网格列表。
- Error：加载失败，显示错误提示与重试按钮。

状态切换逻辑：
1. 进入页面 → 发送请求 → 状态 = Loading
2. 请求成功 → 状态 = Success → 展示图片
3. 请求失败 → 状态 = Error → 展示错误页面
4. 点击重试 → 重新请求 → 回到 Loading

### 6. Coil 图片加载
Coil 是基于 Kotlin 协程的图片加载库，具有：
- 轻量
- 高性能
- 自带缓存
- Compose 友好

使用 AsyncImage 加载网络图片：
AsyncImage(model = url, contentDescription = null)

### 7. 网格布局 LazyVerticalGrid
用于大量图片的高效展示，只加载屏幕内可见项，避免卡顿。
本实验使用两列网格展示图片书架。

## 五、实验步骤

### 1. 创建项目并添加依赖
创建 Empty Activity 项目，添加以下依赖：
- Retrofit
- Gson Converter
- Coil
- ViewModel Compose

添加网络权限：
<uses-permission android:name="android.permission.INTERNET" />

### 2. 创建数据模型
- BookDto：网络数据结构
- Book：UI 使用的领域模型
- 实现 DTO 转模型扩展函数

### 3. 搭建网络层
创建 Retrofit 实例，创建 ApiService 接口。

### 4. 实现 Repository
创建接口与网络、离线两个实现类。

### 5. 创建依赖注入容器 AppContainer
统一管理 ApiService、Repository 实例。

### 6. 创建 Application 类
提供全局 AppContainer 实例。

### 7. 实现 ViewModel 与状态管理
定义 sealed interface 状态。
在 viewModelScope 中调用 Repository 获取数据。

### 8. 实现 Compose UI
- 顶部标题栏
- 加载中动画
- 错误页面与重试按钮
- 两列图片网格
- 点击弹窗查看大图

### 9. 测试运行
- 网络正常 → 显示图片
- 关闭网络 → 显示兜底数据
- 加载失败 → 显示错误界面
- 点击图片 → 弹出大图

## 六、实验结果
1. 应用成功从 Apifox Mock 接口获取图片数据。
2. 图片以两列网格形式展示，布局美观。
3. 加载状态、成功状态、失败状态切换正常。
4. 断网状态下自动显示兜底数据，不崩溃。
5. 点击图片可弹出详情弹窗，体验流畅。
6. 图片加载使用 Coil，缓存有效，滑动流畅。
7. 整体架构清晰，分层合理，符合 Android 官方标准架构。

## 七、实验中遇到的问题及解决方法

### 问题 1：网络请求失败
原因：未添加 INTERNET 权限。
解决：在 AndroidManifest.xml 中添加权限。

### 问题 2：img_src 字段无法解析
原因：JSON 字段是 img_src，Kotlin 变量是 imgSrc，名称不匹配。
解决：使用 @SerializedName(“img_src”) 标注。

### 问题 3：图片无法显示
原因：链接错误或 Coil 依赖未添加。
解决：检查 URL，确认添加 Coil 依赖。

### 问题 4：ViewModel 无法获取 Repository
原因：未使用 Application 与 Container 提供实例。
解决：在 BookshelfApplication 中初始化 AppContainer。

### 问题 5：加载失败时没有重试按钮
解决：在 Error 状态中添加重试按钮，并调用 ViewModel 的 reload 方法。

## 八、实验总结
本次实验完整实现了一个网络图片书架应用，涵盖了 Android 开发中最核心的技术：
网络请求、JSON 解析、协程、Repository、依赖注入、状态管理、图片加载、UI 构建。

通过实验，我深刻理解了：
1. 网络层如何与 UI 层分离。
2. 状态管理如何让应用更稳定。
3. 架构模式如何提高代码可维护性。
4. 工程化项目如何处理加载、失败、异常、重试。

实验达到了预期目标，完成了一个可正常运行、体验良好、结构规范的网络图片书架应用。