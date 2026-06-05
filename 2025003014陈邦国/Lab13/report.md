# Lab13：创建 Bookshelf 网络书架应用 实验报告
## 1. 实验基本信息
- 实验名称：创建 Bookshelf 网络书架应用
- 开发环境：Android Studio Hedgehog、Kotlin、Jetpack Compose、Gradle
- 技术栈：Retrofit + Gson + Coil + ViewModel + Repository + 协程
- 接口地址：https://m1.apifoxmock.com/m1/8321477-8085280-default/photos

## 2. 改用 Apifox Mock 接口的原因
1. Mock 接口提供稳定、公开的测试 JSON 数据，无需部署真实后端服务，降低实验环境搭建成本；
2. 接口支持 HTTPS 协议，Android 高版本无需额外配置明文网络访问规则，兼容性更好；
3. 接口返回格式固定（id + img_src），结构简单，专注练习**网络请求、数据解析、状态管理、图片加载**核心知识点；
4. 数据可实时访问，不受本地静态资源限制，完整模拟真实 App 网络请求流程。

## 3. Retrofit 服务接口定义说明
1. 定义 `BookshelfApiService` 接口，使用 Retrofit 注解 `@GET` 声明网络请求路径 `photos`；
2. 接口方法使用 `suspend` 挂起函数，配合 Kotlin 协程实现**同步风格异步网络请求**，避免回调地狱；
3. 结合 Gson 转换器，自动将服务端 JSON 数组解析为本地 `BookDto` 数据类；
4. 统一配置基础地址 `BASE_URL`，便于后期接口地址维护与修改。

## 4. Repository 分层与数据源隔离原理
1. 抽象 `BooksRepository` 顶层接口，统一定义**获取书籍列表、根据ID查询单本书籍**两个核心方法；
2. 实现两个数据源类：
   - `NetworkBooksRepository`：调用 Retrofit 发起真实网络请求，对接 Apifox Mock 接口；
   - `OfflineBooksRepository`：内置静态模拟数据，作为断网、网络异常时的兜底数据源；
3. 通过 `AppContainer` 统一创建 Retrofit、ApiService、Repository 实例，实现简单依赖注入；
4. ViewModel 仅依赖 Repository 接口，不直接耦合 Retrofit 网络层，**实现数据源解耦**，切换网络/离线数据源无需修改 ViewModel 和 UI 代码，符合分层架构思想。

## 5. UI 状态（Loading / Success / Error）切换逻辑
使用**密封接口 `BookshelfUiState`** 统一管理页面状态，三种状态切换流程如下：
1. **Loading 加载状态**：页面初始化时，ViewModel 触发数据加载，UI 展示圆形进度指示器；
2. **Success 成功状态**：网络/离线数据加载完成后，状态切换为 Success，UI 通过 `LazyVerticalGrid` 展示图片网格；点击网格条目，弹出详情弹窗展示大图与编号；
3. **Error 错误状态**：网络请求异常、接口访问失败时，状态切换为 Error，UI 展示错误文本 + 重试按钮；点击重试按钮，重新触发数据加载流程，回到 Loading 状态。

所有网络操作均在 `viewModelScope` 协程中执行，保证页面销毁时自动终止请求，避免内存泄漏。

## 6. 运行效果说明
1. 正常联网：App 从 Apifox Mock 接口拉取远程图片，网格列表正常展示，点击条目弹出详情弹窗；
2. 断开网络：App 自动切换为离线兜底数据，页面仍可正常展示，功能不受断网影响；
3. 图片加载：全部使用 Coil `AsyncImage` 加载远程图片，支持缩放、裁剪；
4. 异常处理：网络超时、接口报错时，页面展示错误提示与重试按钮，交互友好。

## 7. 实验遇到的问题及解决方案
### 问题1：远程图片无法加载
- 原因：未添加网络权限 `<uses-permission android:name="android.permission.INTERNET" />`；
- 解决：在 `AndroidManifest.xml` 中补充网络访问权限。

### 问题2：JSON 字段解析失败
- 原因：服务端字段为 `img_src`，本地数据类字段命名不匹配；
- 解决：使用 Gson 注解 `@SerializedName("img_src")` 做字段映射。

### 问题3：网络请求导致页面卡顿、崩溃
- 原因：在 Compose 组件主线程直接发起网络请求；
- 解决：网络请求统一放在 Repository 中，由 ViewModel 的 `viewModelScope` 协程调度执行。

### 问题4：断网后页面空白
- 原因：无兜底离线数据源；
- 解决：实现 `OfflineBooksRepository`，网络异常时自动切换静态模拟数据。

## 8. 实验总结
本次实验完整实现了 **Android 经典分层架构：UI层(Compose) → 业务层(ViewModel) → 数据层(Repository) → 网络层(Retrofit)**。熟练掌握了 Retrofit 网络请求、Gson 数据解析、Coil 图片加载、UI 状态管理、协程使用以及数据源解耦思想，理解了 Repository 模式在 Android 项目中的作用，具备开发基础网络类 Compose 应用的能力。