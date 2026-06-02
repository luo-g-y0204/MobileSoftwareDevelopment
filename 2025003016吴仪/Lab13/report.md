# Lab13：创建Bookshelf网络书架应用
姓名：吴仪
学号：2025003016

---

## 一、应用功能与展示信息
本实验基于Jetpack Compose+Retrofit+MVVM架构开发图书书架项目，核心功能如下：
- 采用**网格列表**展示20本图书数据，每一项包含图书封面+图书名称。
- 支持**点击卡片弹窗查看大图详情**，点击关闭按钮收起弹窗。
- 双数据源自动切换：优先请求Apifox Mock在线接口，网络异常自动切换本地离线20条图书数据。
- 页面三种状态管控：加载中Loading、数据成功Success、异常Error，分别展示加载圈、列表、错误提示。
- 使用Coil实现图片异步网络加载，适配Android权限规范。

## 二、实验背景与核心目标
本实验围绕网络数据请求+MVVM分层思想，借助Apifox Mock模拟后端接口，完成从网络拉取数据到Compose页面渲染全流程，目标如下：
1. 掌握Retrofit定义接口、网络请求、Gson解析JSON数据。
2. 区分DTO网络实体与领域Model，实现数据转换，避免接口字段直接暴露UI层。
3. 使用Repository仓库模式隔离数据源，实现网络/离线两套数据无缝切换。
4. 借助AppContainer依赖容器统一管理所有依赖实例，完成依赖注入解耦。
5. 在ViewModel中通过StateFlow管控页面多状态，协程处理异步请求，不在Compose直接发起网络调用。
6. Compose网格布局开发、弹窗详情、图片异步加载等UI开发。

## 三、核心实现说明
### 1. Apifox Mock接口选用说明
实验选用Apifox在线Mock接口作为后端数据源，相比真实线上接口优势：接口稳定可控、自定义返回JSON格式、不受第三方接口改版失效影响，方便本地调试。BASE_URL固定，接口地址末尾携带`/`保证Retrofit路径拼接正确。

### 2. Retrofit服务接口定义
在`BookshelfApiService`中使用`suspend`挂起函数定义GET请求接口，适配Kotlin协程，Retrofit配合GsonConverter自动完成JSON→BookDto实体序列化，`@SerializedName`注解处理接口字段和实体字段名称不一致问题。

### 3. Repository分层隔离数据源
1. `BooksRepository`为抽象接口，统一定义`getBooks()`数据获取方法。
2. `NetworkBooksRepository`：实现接口，通过Retrofit调用网络接口，将DTO转为领域Book对象。
3. `OfflineBooksRepository`：本地离线数据源，内置20条带国内可用图片链接的图书数据，网络报错时自动降级使用。
4. 所有Repository实例统一由`AppContainer`依赖容器创建管理。

### 4. AppContainer依赖容器作用
作为全局依赖注入容器，集中初始化Retrofit Api、Repository实例，Application启动时创建容器并全局持有，MainActivity从Application获取容器，将Repository注入ViewModel，实现依赖与业务代码解耦。

### 5. UI三种状态切换逻辑
通过密封接口`BookshelfUiState`定义三种页面状态：
- Loading：初始化/刷新数据时，页面展示圆形加载指示器；
- Success：数据请求成功，渲染2列网格图书列表；
- Error：网络异常/解析失败，页面展示错误文本。
ViewModel通过StateFlow保存状态，UI层collect收集状态自动刷新页面。

## 四、运行截图与遇到的问题
### 1. 运行效果
应用正常启动，联网状态加载在线接口数据，断网自动加载离线20本图书，全部图片正常加载，点击任意图书卡片弹出大图详情弹窗。

### 2. 项目踩坑记录
1. **图片空白不显示**：最初使用picsum境外图片地址国内无法访问，替换国内百度图片链接后图片正常加载；遗漏`INTERNET`网络权限，在AndroidManifest添加权限后修复。
2. **卡片点击无效**：初始clickable内部空实现，修改为点击赋值选中图书、唤起详情弹窗，解决点击无响应。
3. **collectAsStateWithLifecycle爆红**：缺少对应lifecycle依赖，补充viewmodel-compose依赖并同步Gradle后报错消除。
4. **模拟器设备找不到**：模拟器异常掉线，重启模拟器后正常安装运行APP。