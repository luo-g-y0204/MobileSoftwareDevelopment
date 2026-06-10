## 一、名片展示的个人信息
本次使用 Jetpack Compose 实现的个人电子名片展示内容如下：
1. 个人头像（资源文件：R.drawable.touxiang）
2. 姓名：覃俊杰
3. 职位：Android开发工程师
4. 手机号：+86 176-7730-5156
5. 社交昵称：@qinjunjie
6. 邮箱：398298834@qq.com

## 二、布局结构简要说明
整个界面采用 Jetpack Compose 声明式 UI 编写，使用组件化思想将界面拆分为多个可组合函数，布局清晰、层次分明。

1. **BusinessCard（根布局）**
   - 使用 Column 作为根容器，设置 `fillMaxSize()` 填充全屏
   - 背景色配置为深蓝色（Color(0xFF073042)）
   - 整体内容水平居中（horizontalAlignment = Alignment.CenterHorizontally），垂直方向两端分布（verticalArrangement = Arrangement.SpaceBetween）
   - 分为上半部分 CardTop 和下半部分 CardBottom 两大模块

2. **CardTop（上半部分信息区）**
   - 使用 Column 垂直排列，宽度填充全屏（fillMaxWidth()）
   - 顶部预留 60dp 内边距，内容水平居中对齐
   - 包含核心组件：120dp 尺寸圆形头像、姓名文本、职位文本
   - 头像与姓名间设置 24dp 间距，姓名与职位间设置 8dp 间距
   - 文字样式区分：姓名（白色、28sp、加粗），职位（强调色、18sp）

3. **CardBottom（下半部分联系方式区）**
   - 使用 Column 纵向排列，宽度填充全屏，底部预留 60dp 内边距
   - 包含三行联系方式，每行均使用 ContactRow 组件，行与行之间添加半透明灰色分割线（Color.Gray.copy(alpha = 0.5f)）
   - 涵盖手机号、邮箱、社交昵称三类联系方式，分别对应 Phone、Email、Share 图标

4. **ContactRow（单行联系方式）**
   - 使用 Row 横向排列，宽度填充全屏，纵向内边距 12dp、横向内边距 40dp
   - 左侧为 Material 图标（24dp 尺寸），右侧为文字信息
   - 图标与文字间设置 16dp 水平间距，且垂直居中对齐（verticalAlignment = Alignment.CenterVertically）
   - 图标使用强调色（accentColor），文字为白色（16sp）

## 三、遇到的问题和解决过程
1. **圆形头像实现问题**
    - **问题**：默认图片为矩形，直接展示不符合电子名片的设计要求，界面不够美观。
    - **解决**：使用 `Modifier.clip(CircleShape)` 将图片裁剪为圆形，配合 `ContentScale.Crop` 确保图片比例协调、不变形。

2. **图标与文字对齐问题**
    - **问题**：联系方式栏中，图标与文字默认无法垂直居中，导致布局显示杂乱、不整齐。
    - **解决**：在 Row 布局容器中设置 `verticalAlignment = Alignment.CenterVertically`，使图标与文字实现精准垂直居中对齐。

3. **资源引用与程序闪退问题**
    - **问题**：头像图片资源存放路径错误或文件名不匹配，导致应用运行时崩溃、无法正常启动。
    - **解决**：检查并确保 `touxiang` 图片正确放置在 `drawable` 目录下，清理项目缓存后重新编译运行。

4. **界面样式与布局优化问题**
    - **问题**：界面各组件颜色、间距缺乏统一规范，导致视觉效果差、层次不清晰。
    - **解决**：定义全局颜色常量实现样式统一；合理设置组件内外边距与间距；使用 `Divider` 分割线优化界面结构与美观度。

## 四、实验总结
通过本次实验，我成功使用 Jetpack Compose 完成了个人电子名片的开发。核心收获如下：
1. 掌握 Column、Row 基础布局的对齐方式（horizontalAlignment/verticalAlignment）、排列方式（Arrangement）配置，以及 SpaceBetween 等垂直排列策略的实际应用；
2. 学会定义全局常量管理颜色、尺寸等样式属性，提升代码可维护性；
3. 熟练使用 Modifier 修饰符实现组件尺寸、间距、裁剪、背景等样式定制，尤其是圆形头像的裁剪实现；
4. 理解声明式 UI 的组件化思想，能够将界面拆分为 BusinessCard、CardTop、CardBottom、ContactRow 等职责单一的可组合函数，并通过参数传递实现组件复用；
5. 掌握 Material 图标（Icons.Default.Phone/Email/Share）的使用，以及分割线（Divider）、间距（Spacer）等辅助组件的实战应用；
6. 学会使用 @Preview 注解实现界面实时预览，提升开发效率。

本次实验为后续复杂 Compose 界面开发奠定了扎实的基础，也形成了「样式常量化、组件原子化、布局规范化」的开发思路。