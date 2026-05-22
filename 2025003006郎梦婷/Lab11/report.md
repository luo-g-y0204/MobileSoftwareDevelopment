# Lab11 自适应布局实验报告

## 1. WindowSizeClass 概念
WindowSizeClass 是 Jetpack Compose 提供的屏幕尺寸分类工具，用于根据屏幕宽度/高度自动区分设备类型，实现自适应布局。

WindowWidthSizeClass 三种宽度类型：
- Compact：手机竖屏，最常用的小屏设备
- Medium：折叠屏、小平板
- Expanded：平板、横屏大手机，适合双窗格并排布局

## 2. SportsContentType 设计思路
定义 ListOnly 和 ListAndDetail 两种类型：
- ListOnly：小屏设备，列表与详情只能显示一个，通过页面切换导航
- ListAndDetail：大屏设备，同时显示列表与详情，提升空间利用率与操作效率

作用：统一管理布局模式，让代码逻辑更清晰。

## 3. SportsListAndDetails 布局设计
使用 Row 实现左右并排：
- 左侧列表 weight(1f)
- 右侧详情 weight(2f)
比例设计原因：
- 列表只需展示标题与简介，无需过宽
- 详情需要更大空间展示图片与文字
符合 Material Design 列表-详情规范。

## 4. SportsAppBar 行为差异设计
- 大屏模式：始终显示 Sports，无返回按钮（列表始终可见）
- 小屏模式：详情页显示返回按钮，用于回到列表页
保证不同屏幕下的导航逻辑一致。

## 5. 返回键处理策略
- 小屏：详情页返回列表页
- 大屏：列表与详情同时显示，返回应直接退出应用
符合用户直觉操作，避免无效返回。

## 6. 实验问题与解决
1. 问题：大屏模式下返回键仍跳回列表
   解决：使用 BackHandler 直接 finish Activity
2. 问题：AppBar 在大屏仍显示返回按钮
   解决：增加 isListAndDetail 参数控制显隐
3. 问题：并排布局比例错乱
   解决：使用 Modifier.weight 正确分配权重