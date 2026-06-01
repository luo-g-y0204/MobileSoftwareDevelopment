# Lab11 报告：为 Sports 应用添加大屏自适应布局

## 1. WindowSizeClass 概念简介
WindowSizeClass 是 Jetpack Compose（Material3）提供的用于描述应用可视窗口大小的分类机制，常用于实现自适应布局。针对宽度有 `WindowWidthSizeClass`，其三个值和典型适用设备：

- `Compact`：窄屏（典型手机竖向或小屏设备）。
- `Medium`：中等宽度（大型手机或小平板）。
- `Expanded`：展开宽度（典型平板、桌面或折叠设备展开态）。

通过 `calculateWindowSizeClass(activity)` 获取当前窗口类别，然后根据 `widthSizeClass` 切换布局。

## 2. SportsContentType 设计思路
`SportsContentType` 枚举包含两种类型：

- `ListOnly`：单窗格模式。用于手机等窄屏场景，列表与详情页面分离，用户在列表中选择项后导航到详情页。
- `ListAndDetail`：并排双窗格模式。用于大屏场景，同时展示左侧列表与右侧详情，提升信息密度与交互效率。

使用枚举的好处是将尺寸判断与内容呈现解耦，便于在多个组件间传递并保持一致行为。

## 3. SportsListAndDetails 布局设计说明
在大屏（`ListAndDetail`）模式下，采用 `Row` 将列表与详情并排显示；使用 `Modifier.weight()` 分配空间比例：

- 列表（左侧）使用 `weight(1f)`，约占 1/3 的空间，用于快速浏览与选择。
- 详情（右侧）使用 `weight(2f)`，约占 2/3 的空间，给详情内容更多展示面积。

此比例兼顾导航与内容显示：列表仍保持可读性，而详情区域有充足空间展示图片与文本。

## 4. SportsAppBar 在大屏/小屏下行为差异
设计要点：

- 在大屏（`ListAndDetail`）下，`TopAppBar` 始终显示应用标题（`Sports`），且不显示返回按钮，因为列表始终可见，没有页面层级需要返回。
- 在小屏（`ListOnly`）下，保持原有行为：列表页显示 `Sports`，详情页显示 `Sport Info`（或详情标题）；详情页显示返回按钮以返回列表页。

通过在 `SportsAppBar` 中新增 `isListAndDetail` 参数实现条件渲染。

## 5. 返回键处理策略
- 小屏模式：详情页需要返回到列表页，使用 `BackHandler` 拦截系统返回并调用 `navigateToListPage()`。
- 大屏模式：按系统返回键应退出应用（因为用户已在主屏幕且没有“返回上一级”的页面层级），在 `SportsListAndDetails` 中注册 `BackHandler`，调用 Activity 的 `finish()` 以退出。

实现细节：将 `SportsDetail` 的返回拦截设为可选（`enableBackHandler`），在双窗格模式下禁用 `SportsDetail` 的拦截，统一由 `SportsListAndDetails` 处理返回。

## 6. 实验中遇到的问题与解决过程
- 问题：`SportsDetail` 无条件注册 `BackHandler` 会在双窗格模式下拦截返回，导致按返回键无法退出 Activity。
  解决：将 `SportsDetail` 的 `BackHandler` 改为可选（`enableBackHandler` 参数），在双窗格中传入 `false`，并将退出行为集中到 `SportsListAndDetails` 的 `BackHandler`（调用 Activity.finish()）。

- 问题：需要将窗口尺寸信息从 `Activity` 传递到 `Composable`。
  解决：在 `MainActivity` 中使用 `calculateWindowSizeClass(activity = this)`，将 `windowSizeClass.widthSizeClass` 传给 `SportsApp`，`SportsApp` 根据 `WindowWidthSizeClass.Expanded` 切换到双窗格模式。

---

> 注：本提交已实现代码修改与 `report.md`。截图按要求跳过（未包含）。如果需要我可以进一步运行或在项目中添加更详细的注释或测试预览。