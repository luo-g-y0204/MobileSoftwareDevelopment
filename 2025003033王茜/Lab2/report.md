## 一、实验信息
姓名：王茜
学号：2025003033
实验名称：使用 Jetpack Compose 构建名片应用
开发环境：Android Studio + Kotlin + Jetpack Compose
二、名片展示内容
本次实现的电子名片包含以下信息：

## 二、名片展示的个人信息
1. 个人头像
2. 姓名：Wang Xi
3. 职位：Student
4. 联系电话：+18108746132
5. 邮箱地址：1250292825@QQ.com
6. 社交账号：1250292825W
7. 界面整体采用深色背景搭配绿色强调色，风格简洁统一。

## 三、布局结构说明
整体界面使用 Column 作为根布局，垂直分为上下两个区域：
1. 上半部分（CardTop）
使用 Column 纵向排列
依次包含：Image 头像、姓名 Text、职位 Text
通过 Modifier.padding 和 Spacer 控制间距，居中显示
2. 下半部分（CardBottom）
使用 Column 包裹多条联系方式
每条信息封装为 ContactRow 可组合函数
每条 ContactRow 内部使用 Row 横向排列：Icon + Text
使用 HorizontalDivider 增加分隔线，提升界面层次感
核心组件使用
3. 布局：Column、Row
显示：Image、Text、Icon、HorizontalDivider
样式：Modifier 控制大小、边距、背景、填充
图标：使用 Material Icons（Phone、Email、Share）

## 四、实验过程与遇到的问题及解决方法
问题一：Icon / Image 组件无法正常显示
原因：contentDescription 未设置，或图片资源引用错误
解决：检查资源 ID 拼写
问题二：界面元素不居中、排版混乱
原因：未设置 horizontalAlignment 或 verticalArrangement
解决：在 Column 中配置 Alignment.CenterHorizontally 实现水平居中
问题三：颜色显示与预期不一致
原因：色值缺少 FF 前缀（不透明通道）
解决：使用 Color 格式，保证颜色完全不透明
