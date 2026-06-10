# Lab2 Jetpack Compose 名片应用实验报告

## 一、整体布局思路

本次实验使用 **Jetpack Compose** 制作个人名片界面，整体采用**组件化设计**，代码结构清晰。

1. **主界面**

使用 Column 作为根布局，铺满全屏并设置淡绿色背景，让所有内容在屏幕**居中显示**。

界面分为**顶部信息**和**底部联系方式**两部分，中间用间距隔开。

2. **顶部卡片（CardTop）**

显示**头像、姓名、职位**，使用 Column 垂直排列，通过 Spacer 调整元素间距，样式统一美观。

3. **底部联系方式（CardBottom）**

封装了 ContactRow 组件，统一显示**电话、社交账号、邮箱**。

每一行都是**图标 + 文字**的横向排列（Row），对齐整齐、间距规范。

---

## 二、遇到的问题与解决方式

### 问题 1

整体内容无法居中显示，页面内容始终偏向屏幕上方。

### 解决

为最外层 Column 添加：

- verticalArrangement = Arrangement.Center 垂直居中

- horizontalAlignment = Alignment.CenterHorizontally 水平居中

- 配合 fillMaxSize() 实现全屏居中效果

### 问题 2

元素之间间距太紧凑，界面拥挤。

### 解决

使用 Spacer 组件和 padding、spacedBy 调整间距，让布局更舒展。

### 问题 3

图标和文字高低错位，不整齐。

### 解决

在 Row 中加入 verticalAlignment = Alignment.CenterVertically，让图标和文字垂直居中对齐。

---

## 三、实验总结

通过本次实验，我学会了 Compose 的基础布局 Column 和 Row 的使用，掌握了图片、文字、图标组件的基本用法，也理解了**组件拆分与复用**的思想，成功完成了简洁美观的名片界面。
