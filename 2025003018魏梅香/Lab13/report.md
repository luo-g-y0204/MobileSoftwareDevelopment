# Lab13 网络书架应用实验报告

## 一、实验目的
1. 掌握 Jetpack Compose 声明式 UI 组件的基本使用，完成图书展示界面搭建
2. 学会使用 Retrofit 网络请求框架实现 API 数据请求与 JSON 解析
3. 掌握 Coil 图片加载库加载网络图片的方法，实现图片缓存与淡入效果
4. 理解 MVVM 架构思想，使用 ViewModel + StateFlow 实现 UI 状态管理
5. 完成网络书架应用的完整开发，实现图书列表展示、详情弹窗、异常处理功能

## 二、实验环境
- 开发工具：Android Studio
- 开发语言：Kotlin
- 界面框架：Jetpack Compose (Material3)
- 网络依赖：Retrofit2、Gson Converter
- 图片依赖：Coil Compose
- 状态管理：ViewModel、StateFlow、Coroutine

## 三、实验内容
1. 创建 Bookshelf 项目，搭建基础项目分层结构
2. 定义 Book 数据模型类，封装图书 id、标题、封面地址等属性
3. 配置 Retrofit 网络请求框架，定义 API 服务接口与基础地址
4. 实现 Repository 数据仓库层，封装网络数据源与离线兜底数据源
5. 开发 ViewModel 层，管理加载中、成功、错误三种 UI 状态
6. 完成 Compose 主界面开发：
   - 顶部标题栏展示"网络书架"
   - 两列网格布局展示图书卡片
   - 加载中显示进度条，错误时显示提示与重试按钮
7. 实现图书卡片点击交互，弹出详情弹窗展示大图预览
8. 配置 AndroidManifest.xml，添加网络权限与应用类注册

## 四、核心实现
1. **网络层实现**
   - 编写 ApiConfig.kt 配置 Retrofit 实例，指定基础 URL 与 Gson 解析器
   - 定义 BookshelfApiService 接口，声明挂起函数 getBooks() 实现 GET 请求
2. **数据层实现**
   - 定义 BooksRepository 接口，抽象数据获取方法
   - 实现 NetworkBooksRepository 处理网络数据请求与 DTO 转领域模型
   - 实现 OfflineBooksRepository 提供本地模拟数据，作为网络异常兜底
3. **状态管理实现**
   - 定义 BookshelfUiState 密封类，覆盖 Loading/Success/Error 三种状态
   - ViewModel 中通过 StateFlow 发布状态，viewModelScope 处理协程请求
4. **UI 层实现**
   - 使用 Scaffold + CenterAlignedTopAppBar 搭建基础页面结构
   - LazyVerticalGrid 实现两列网格布局，Card 组件封装图书卡片
   - AsyncImage 加载网络图片，支持淡入动画与内容描述
   - AlertDialog 实现图书详情弹窗，支持关闭交互

## 五、运行结果
1. 应用启动后自动加载网络图书数据，展示加载进度条
2. 数据加载完成后，以两列网格形式展示所有图书封面与标题
3. 点击任意图书卡片，弹出详情弹窗展示图书大图预览
4. 网络异常时显示错误提示文本与重试按钮，点击重试重新加载数据
5. 断网场景下自动切换为离线模拟数据，保证应用正常运行

## 六、问题与解决
1. 包结构错误：关闭 Android Studio "Compact Middle Packages" 选项，修正 ui 包层级结构
2. Compose API 警告：给 BookshelfScreen 函数添加 @OptIn(ExperimentalMaterial3Api::class) 注解，消除实验性 API 警告
3. StateFlow 收集报错：将 collectAsStateWithLifecycle 替换为稳定版 collectAsState 方法
4. ViewModel 构造报错：修正 ViewModel 构造函数，添加 BooksRepository 参数，统一依赖注入方式
5. 清单文件报错：在 Application 标签中注册 BookshelfApplication 类，添加 INTERNET 网络权限

## 七、实验总结
本次实验完成了基于 Jetpack Compose 的网络书架应用开发，完整实现了网络数据请求、图片加载、状态管理、异常处理等核心功能。通过本次实验，熟练掌握了 Retrofit 网络请求、Coil 图片加载、ViewModel + StateFlow 状态管理等 Android 现代开发技术，理解了 MVVM 分层架构的设计思想。在解决开发过程中的包结构、API 兼容、依赖注入等问题时，提升了问题排查与 Debug 能力，完成了课程实验的全部要求。