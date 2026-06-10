# Lab2 Jetpack Compose 名片应用实验报告

## 一、整体布局思路
主要展示了个人核心信息，包括个人头像、姓名、职业，以及三项联系方式：电话号码、社交账号和邮箱地址，整体信息清晰完整。
整个界面使用 Jetpack Compose 实现，以 Column 作为最外层的主布局，实现整体内容的垂直排列与居中显示。
界面分为上下两个模块：上方通过 CardTop 组件展示头像、姓名和职业，内部同样使用 Column 垂直排列；下方通过 CardBottom 组件展示联系方式，内部使用 Column 排列多行信息。
为了统一样式，将每一行 “图标 + 文字” 封装成 ContactRow 组件，组件内部使用 Row 实现图标与文字的横向排列，整体布局结构清晰、嵌套合理、易于维护。

## 二、遇到的问题与解决方式
### 问题 1
整体内容无法居中显示，页面内容一直偏向屏幕上方。
### 解决
为主布局 Column 设置了 verticalArrangement = Arrangement.Center 和 horizontalAlignment = Alignment.CenterHorizontally，让整个名片在屏幕中居中展示。

### 问题 2
顶部头像、姓名、职位间距过于紧凑，导致界面拥挤。
### 解决
通过 Spacer 组件和 padding 修饰符添加间距和内边距，让各元素之间保持合适距离，层次更加分明。

### 问题 3
在底部联系方式中，出现了图标和文字高低错位的问题。
### 解决
为 ContactRow 内部的 Row 布局设置了 verticalAlignment = Alignment.CenterVertically，让图标和文字保持在同一水平线上。