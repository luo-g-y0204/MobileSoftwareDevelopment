# Lab2 Jetpack Compose 名片应用实验报告

## 一、名片展示的个人信息
- 个人头像
- 姓名：格玛丁争
- 职位：Android开发者
- 手机号：18608879139
- 社交昵称：坤人格
- 邮箱：486181734@qq.com

## 二、布局结构简要说明
1. **BusinessCard（根布局）**
   - 使用 Column 作为根容器
   - 设置全屏大小，背景色为 0xFFDDEEFF
   - 整体内容水平居中、垂直居中
   - 分为上下两个区域：CardTop 和 CardBottom

2. **CardTop（上半部分）**
   - 使用 Column 垂直布局
   - 包含圆形头像、姓名、职位
   - 所有内容水平居中

3. **CardBottom（下半部分）**
   - 使用 Column 纵向排列
   - 包含三行联系方式，均使用 ContactRow 组件

4. **ContactRow（单行联系方式）**
   - 使用 Row 横向排列
   - 左侧图标，右侧文字
   - 图标与文字垂直居中对齐

## 三、遇到的问题和解决过程
1. **圆形头像实现问题**
   - 问题：图片默认是矩形，视觉效果差
   - 解决：使用 Modifier.clip(CircleShape) 裁剪为圆形，配合 ContentScale.Crop 防止变形。

2. **图标与文字对齐问题**
   - 问题：图标和文字高低不一，界面不整齐
   - 解决：在 Row 中设置 verticalAlignment = Alignment.CenterVertically 实现居中对齐。

3. **界面样式统一问题**
   - 问题：图标、文字颜色杂乱
   - 解决：统一使用绿色 0xFF3DDC84 作为主题色，风格保持一致。

4. **布局间距问题**
   - 问题：上下部分距离太近，界面拥挤
   - 解决：添加 Spacer(100.dp) 拉开间距，提升美观度。

5. **图片资源无法显示问题**
   - 问题：头像无法加载
   - 解决：确认图片名称正确，放在 drawable 文件夹，重新编译项目。

## 四、实验总结
本次实验使用 Jetpack Compose 完成了电子名片界面开发。
掌握了 Column、Row 基础布局，学会使用 Image、Icon、Text、Spacer 等组件，能够熟练使用 Modifier 实现裁剪、边距、大小、背景等样式设置。
通过组件化拆分界面，理解了 Compose 声明式 UI 的思想，为后续 Android 界面开发打下基础。