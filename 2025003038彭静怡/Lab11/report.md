# Lab11 实验报告：为 Sports 应用添加大屏自适应布局

## 一、实验概述

本次实验基于 Jetpack Compose 中的 `material3-window-size-class` 库，为 Sports 运动资讯应用添加了大屏自适应布局功能。实现了在手机（紧凑宽度）上使用单窗格布局，在平板（展开宽度）上使用"列表-详情"并排双窗格布局。

---

## 二、WindowSizeClass 概念简介

`WindowSizeClass` 是 Jetpack Compose 提供的用于检测设备屏幕尺寸的工具类，它将窗口尺寸分为不同的类别：

| 宽度类别 | 范围 | 适用设备 |
|---------|------|---------|
| `Compact` | < 600dp | 手机竖屏 |
| `Medium` | 600dp - 840dp | 手机横屏、小型平板 |
| `Expanded` | >= 840dp | 标准平板、桌面设备 |

`WindowWidthSizeClass` 专注于宽度方向的尺寸分类，这对于决定是否采用并排布局至关重要。

---

## 三、SportsContentType 枚举设计思路

在 `utils/WindowStateUtils.kt` 中已定义的枚举：

```kotlin
enum class SportsContentType {
    ListOnly,      // 单窗格：只显示列表或详情
    ListAndDetail  // 双窗格：同时显示列表和详情
}
```

**设计理由**：
- **ListOnly**：适用于小屏设备，用户需要在列表页和详情页之间切换
- **ListAndDetail**：适用于大屏设备，可同时展示列表和详情，提升用户体验

---

## 四、SportsListAndDetails 布局设计

### 4.1 布局结构

```kotlin
@Composable
private fun SportsListAndDetails(
    sports: List<Sport>,
    currentSport: Sport,
    onSportClick: (Sport) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        SportsList(
            sports = sports,
            onClick = onSportClick,
            modifier = Modifier.weight(1f),
            contentPadding = contentPadding
        )
        SportsDetail(
            selectedSport = currentSport,
            onBackPressed = {},
            contentPadding = contentPadding,
            modifier = Modifier.weight(2f)
        )
    }
}
```

### 4.2 比例分配理由

- **列表占 1/3 (weight=1)**：列表作为导航区域，不需要太宽，能显示完整列表项即可
- **详情占 2/3 (weight=2)**：详情包含更多内容（图片、描述等），需要更大的展示空间
- 这种 1:2 的比例符合 Material Design 的列表-详情布局规范

---

## 五、SportsAppBar 大屏/小屏行为差异

### 5.1 大屏模式 (`ListAndDetail`)

| 属性 | 行为 |
|------|------|
| 标题 | 始终显示 "Sports" |
| 返回按钮 | 不显示 |

**设计考虑**：大屏模式下列表和详情同时可见，用户无需"返回"操作，因此不需要返回按钮，标题保持统一。

### 5.2 小屏模式 (`ListOnly`)

| 属性 | 列表页 | 详情页 |
|------|--------|--------|
| 标题 | "Sports" | "Sport Info" |
| 返回按钮 | 不显示 | 显示 |

**设计考虑**：小屏模式下用户在列表和详情之间切换，详情页需要返回按钮回到列表。

---

## 六、返回键处理策略

### 6.1 小屏模式

在 `SportsDetail` 中使用 `BackHandler` 返回列表页：

```kotlin
BackHandler {
    onBackPressed()  // 返回列表页
}
```

### 6.2 大屏模式

在 `SportsListAndDetails` 中使用 `BackHandler` 退出应用：

```kotlin
val context = LocalContext.current
BackHandler {
    (context as Activity).finish()  // 退出应用
}
```

**行为差异原因**：
- 小屏模式下，用户在详情页按返回键应回到列表页
- 大屏模式下，列表和详情同时显示，用户已经在"主界面"，按返回键应退出应用

---

## 七、核心代码修改

### 7.1 MainActivity.kt 修改

1. 添加 `calculateWindowSizeClass` 导入
2. 在 `setContent` 中计算窗口尺寸类别
3. 将 `widthSizeClass` 传入 `SportsApp`

### 7.2 SportsScreens.kt 修改

1. 修改 `SportsApp` 函数签名，接收 `windowWidthSizeClass` 参数
2. 根据宽度类别判断 `contentType`
3. 创建 `SportsListAndDetails` 双窗格布局组件
4. 修改 `SportsAppBar` 添加 `isListAndDetail` 参数

---

## 八、实验验证清单

| 验证项 | 状态 |
|--------|------|
| 小屏（手机）单窗格导航 | ✅ |
| 大屏（平板）双窗格并排显示 | ✅ |
| 大屏点击列表项更新详情 | ✅ |
| 大屏应用栏显示 "Sports" | ✅ |
| 大屏无返回按钮 | ✅ |
| 大屏按返回键退出应用 | ✅ |
| 小屏详情页有返回按钮 | ✅ |
| 浅色/深色模式正常 | ✅ |

---

## 九、实验中遇到的问题与解决

### 问题 1：WindowSizeClass API 为实验性 API

**解决**：在使用的函数和类上添加 `@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)` 注解

### 问题 2：大屏模式下返回键行为不正确

**解决**：在 `SportsListAndDetails` 中单独使用 `BackHandler`，直接调用 `Activity.finish()` 退出应用

### 问题 3：双窗格布局比例不合理

**解决**：通过调整 `Modifier.weight()` 的值，将列表设为 1，详情设为 2，达到合理的 1:2 比例

---

## 十、总结

通过本次实验，我掌握了：

1. 使用 `calculateWindowSizeClass()` 获取设备窗口尺寸类别
2. 根据 `WindowWidthSizeClass` 判断设备类型并切换布局
3. 实现"列表-详情"并排布局
4. 处理不同布局模式下的 TopAppBar 行为
5. 正确处理不同模式下的返回键行为

自适应布局能够显著提升大屏设备的用户体验，让用户同时看到列表和详情内容，减少导航操作。