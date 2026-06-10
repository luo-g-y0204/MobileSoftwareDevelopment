Android电子名片

一、展示的个人信息
个人头像：页面上半部分居中展示动漫头像。
姓名：万文聪
职位：Android 开发工程师
联系方式：
    手机号码：19174245977
    社交账号：@WanWenCong
    电子邮箱：2779739612@qq.com

二、布局结构与 Composable 说明

本次实验采用 Jetpack Compose 进行声明式 UI 开发，整体布局清晰，组件化程度高。

1. 整体布局
最外层容器：使用 Column 垂直布局。
背景色：设置为淡青色 Color(0xFFE0F2E9)，通过 background 修饰符应用。
对齐方式：horizontalAlignment = Alignment.CenterHorizontally，verticalArrangement = Arrangement.Center，确保卡片在屏幕中央垂直居中。
区域划分：通过 Spacer(modifier = Modifier.height(100.dp)) 在中间留出空白间隔，将界面逻辑划分为上下两个独立区域：CardTop 和 CardBottom。

2. 上半部分（个人信息）
组件：Image + Text (姓名) + Text (职位)。
排版：Column 内部居中对齐。
样式细节：
    姓名：字号 52.sp，字重 FontWeight.Light，黑色。
    职位：字号 20.sp，颜色 Color(0xFF87CEEB)（浅蓝色），字重 FontWeight.Medium。

3. 下半部分（联系方式）
容器：Column，通过 fillMaxWidth() 铺满宽度，并设置左右 40.dp 内边距。
行组件：每行使用 Row 水平布局。
结构：Icon -> Spacer(24.dp) -> Text。
图标：使用 Material Icons (Phone, Share, Email)，颜色与职位文字保持一致，均为浅蓝色。

4. 核心组件与技术点
布局组件：Column (垂直布局), Row (水平布局), Spacer (间距控制)。
展示组件：Image (加载资源图片), Text (文本渲染), Icon (矢量图标)。
修饰符 (Modifier)：fillMaxSize (填充父布局), padding (内边距), size (尺寸控制), background (背景色)。
主题与样式：使用 MaterialTheme 包裹，利用 Color 和 FontWeight API 实现精确的视觉定制。

三、遇到的问题与解决过程

在开发过程中，针对代码实现和视觉效果，我主要解决了以下几个问题：

1. 布局嵌套与对齐逻辑
问题：初期尝试将所有内容堆叠在同一个 Column 中，导致上下区域紧贴，视觉上拥挤。
解决：采用组件化拆分思想，将界面拆分为 CardTop 和 CardBottom 两个独立的 Composable 函数。并在两者之间插入固定高度的 Spacer，利用 Arrangement.Center 使整体布局在屏幕中更加舒展、留白合理。

2. 图标资源缺失
问题：代码中使用了 Icons.Default.Phone 等矢量图标。如果未导入相关依赖或误用 API，会导致编译报错。
解决：确认导入了 androidx.compose.material3:material3 依赖，并正确引用 Icons 相关包，确保图标能正常渲染。

3. 色彩搭配优化
问题：最初使用纯白色或深色背景，导致浅蓝色文字（职位、图标）对比度不够或视觉冲击力过强。
解决：将背景色调整为柔和的淡青色 0xFFE0F2E9，配合浅蓝色的辅助色，营造出清新、专业的科技感风格。

四、实验总结

本次实验通过 Jetpack Compose 实现了一个结构清晰、美观的电子名片界面。我深入理解了 Compose 的声明式 UI 编程范式，熟练掌握了 Column、Row、Modifier 等核心布局工具的使用。在开发过程中，我学会了如何通过组件拆分（如 CardTop、ContactRow）来提高代码的复用性和可读性，并解决了图片显示、布局对齐及色彩搭配等实际问题。最终成功运行并展示了包含个人头像、姓名、职位及联系方式的完整电子名片，达到了预期的实验目标。