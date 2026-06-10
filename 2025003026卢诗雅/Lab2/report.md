# Lab2 ：使用 Jetpack Compose 构建名片应用

## 一、名片展示的个人信息
本次使用 Jetpack Compose 实现的电子名片展示以下信息：
1. 姓名：卢诗雅
2. 职位：Android 开发工程师
3. 联系电话：+86 138-8738-6676
4. 邮箱：2110131725@qq.com
5. 社交账号：@balaba
6. 个人圆形头像

界面使用浅粉色背景、玫粉色作为强调色，文字为深灰色，整体简洁美观。

---

## 二、布局结构说明
本次使用 Jetpack Compose 实现名片应用，整体分为上下两部分。

### 1. 用到的核心 Composable
- Column：纵向布局，用于页面整体、上半部分、联系方式列表
- Row：横向布局，用于“图标 + 文字”的联系方式行
- Image：显示圆形头像
- Text：显示姓名、职位、联系方式
- Icon：显示电话、邮箱、分享图标
- Spacer：控制组件间距
- Divider：分割线，提升界面层次
- Modifier：控制尺寸、填充、边距、圆形裁剪、背景色

### 2. 组件嵌套结构

布局结构：
BusinessCard（根 Column）
├─ CardTop（Column）
│  ├─ Image（圆形头像）
│  ├─ Text（姓名）
│  └─ Text（职位）
└─ CardBottom（Column）
   ├─ Divider
   ├─ ContactRow（电话）
   ├─ Divider
   ├─ ContactRow（邮箱）
   ├─ Divider
   ├─ ContactRow（社交账号）
   └─ Divider


### 3. 布局思路
- 整体使用 Column + SpaceBetween 实现上下分区
- 上半部分居中展示头像、姓名、职位
- 下半部分使用多行 ContactRow 展示联系方式，并用分割线分隔
- ContactRow 封装为通用组件，实现图标+文字的复用

---

## 三、遇到的问题与解决过程

### 问题1：安装失败 cmd: Can't find service: package
- 原因：ADB 服务异常、模拟器从快照启动导致系统服务丢失
- 解决：
  1. 打开 Device Manager 对模拟器执行 Cold Boot 冷启动
  2. 重启 ADB：adb kill-server / adb start-server
  3. Clean Project → Rebuild Project
- 结果：模拟器恢复正常，App 成功安装运行。

### 问题2：PowerShell 中 adb 命令无法识别
- 原因：ADB 路径未加入系统环境变量
- 解决：使用完整路径执行 adb 命令
- 结果：成功连接设备、卸载旧应用。

### 问题3：头像无法显示为圆形
- 解决：使用 Modifier.clip(CircleShape) 将头像裁剪成圆形。
- 结果：头像正常显示并通过 CircleShape 裁剪为圆形。

### 问题4：图标与文字未对齐
- 原因：Row 未设置垂直居中
- 解决：添加 verticalAlignment = Alignment.CenterVertically
- 结果：图标与文字完美居中对齐。

---


## 四、实验总结
1. 掌握 Jetpack Compose 基础组件：Column、Row、Image、Icon、Text、Spacer、Modifier 等用法。
2. 理解声明式 UI 思想，使用组件化拆分界面，代码结构清晰、可复用。
3. 学会处理 Android 开发常见问题：ADB 异常、模拟器故障、资源缺失、布局对齐等。
4. 能够使用自定义颜色、圆形裁剪、分割线、间距、字体样式完成美观的 UI 界面。
5. 项目可正常编译、安装、运行，完全满足实验要求。


## 四、实验总结
本次实验通过 Jetpack Compose 成功完成了电子名片界面开发，掌握了声明式 UI 的基本写法，熟练使用 Column、Row 进行界面布局，能够运用 Image、Text、Icon、Divider 等组件搭建完整界面。同时学会使用 Modifier 实现裁剪、间距、对齐、背景、填充等样式控制，能够封装可组合函数提升代码复用性，解决了圆形头像、图标与文字对齐、图标上色、模拟器安装失败等常见问题。本次名片设计让我对 Compose 声明式编程有了更清晰的认识，为后续 Android UI 开发打下了扎实基础。