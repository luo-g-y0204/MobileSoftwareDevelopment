# Lab13 Bookshelf 网络图片书架实验报告

## 一、实验目的
1. 掌握Retrofit网络框架发送GET请求，完成JSON解析以及DTO数据转为业务实体。
2. 熟悉分层开发架构：Model、Network、Repository、ViewModel、Compose UI五层架构，实现代码解耦。
3. 学会Coil框架在Compose中异步加载网络图片。
4. 使用ViewModel搭配StateFlow管理页面三种状态：加载中、加载成功、加载失败。
5. 完成网格列表布局与点击弹窗查看详情功能，熟练使用Material3的AlertDialog弹窗控件。

## 二、实验环境
Android Studio：Hedgehog；编译SDK：Android14 API36；最低SDK：API24；开发语言：Kotlin+Jetpack Compose；第三方依赖：Retrofit、Gson、Coil、ViewModel-Compose；运行设备：安卓模拟器。

## 三、实验原理
本项目采用分层架构设计，Model层分为DTO原始数据类和业务实体类，通过扩展函数完成数据转换；Network层借助Retrofit配置接口地址与请求方法，实现网络数据获取；Repository仓库层统一管理数据来源，优先使用网络数据，网络异常自动切换本地离线数据；ViewModel层持有仓库对象，依靠viewModelScope开启协程发起网络请求，通过StateFlow保存页面UI状态和弹窗选中数据；UI层使用Compose声明式布局，LazyVerticalGrid实现自适应图片网格，collectAsStateWithLifecycle监听数据变化自动刷新页面，点击图片唤起详情弹窗。

## 四、实验步骤
1. 项目依赖与权限配置：在app模块build.gradle文件添加Retrofit、Gson、Coil、ViewModel相关依赖；在AndroidManifest.xml中添加网络访问权限<uses-permission android:name="android.permission.INTERNET"/>。
2. 新建model包，创建BookDto接收接口原始数据、Book作为业务实体，编写扩展函数asExternalModel实现DTO转Book。
3. 新建network包，ApiConfig存放接口基地址，BookshelfApiService定义挂起函数getBooks请求接口，通过Retrofit构建实例生成接口对象。
4. 新建data包，创建BooksRepository接口，分别实现网络数据源类NetworkBooksRepository和离线本地数据源OfflineBooksRepository，AppContainer用来全局统一提供仓库实例，自定义Application类保存容器对象。
5. 新建ui包，BookshelfViewModel中使用密封类定义页面UI状态，借助StateFlow存储页面状态和弹窗状态，初始化自动调用方法加载网络数据，提供打开、关闭详情弹窗的方法。
6. 编写Compose页面，LazyVerticalGrid实现自适应多列图片网格，AsyncImage加载网络图片，点击图片弹出AlertDialog弹窗展示大图，MainActivity启动Compose页面。

## 五、关键代码说明
1. 数据转换：利用扩展函数把接口返回的BookDto转为页面使用的Book实体，隔离接口字段改动对页面的影响。
2. 网络请求：使用suspend挂起函数配合协程进行网络请求，不会阻塞主线程。
3. 仓库容错：Repository内部捕获网络异常，断网时自动返回预设本地数据，保证程序不会崩溃。
4. 状态管理：密封类区分Loading、Success、Error三种页面状态，StateFlow实现数据单向流转，页面自动跟随数据刷新。
5. 网格与弹窗：自适应网格根据屏幕宽度自动调整列数，AlertDialog依靠选中的Book对象控制弹窗显示与关闭，符合Compose状态驱动UI思想。

## 六、实验结果
运行APP后，首页先出现加载转圈动画，请求接口成功后页面以自适应网格展示全部网络图片；点击任意一张图片弹出详情弹窗，弹窗内展示原图，点击关闭按钮弹窗消失；断开网络后程序自动加载本地测试图片，功能全部实现。运行截图保存为screenshot.png一并上交。

## 七、问题与解决
1. AlertDialog出现重载参数报错：统一导入material3包下弹窗，严格按照onDismissRequest、title、text、confirmButton顺序填写具名参数，解决重载冲突。
2. 页面组件提示未使用：在MainActivity的setContent代码块中正常调用页面Compose函数，消除未引用警告。
3. 图片无法加载：核对AndroidManifest添加INTERNET网络权限，检查接口地址与图片链接无误后图片正常加载。
4. 页面生命周期异常：使用collectAsStateWithLifecycle替代普通collectAsState，避免页面销毁后无效数据监听。

## 八、实验总结
本次实验完成书架图片浏览项目，熟练掌握MVVM分层架构、Retrofit网络请求、Coil图片加载、Compose状态管理等知识点。分层架构把数据、网络、页面代码拆分，降低模块之间耦合，便于后期维护。编写过程中解决弹窗报错、图片加载失败等问题，加深了对协程异步、StateFlow响应式编程的理解。通过本次实验，能够独立完成从接口请求到页面渲染的完整安卓Compose项目开发，夯实了安卓网络开发基础。

