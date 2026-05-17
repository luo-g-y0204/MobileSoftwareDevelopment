# Lab10 实验报告

## 实验名称

为 Lunch Tray 添加 Jetpack Compose 导航功能

------

## 一、实验概述

本次实验基于 Jetpack Compose Navigation 组件，为 Lunch Tray 点餐应用实现完整的页面导航功能。应用主要包含开始页面、主菜选择页面、配菜选择页面、佐餐选择页面以及订单结算页面。

实验重点在于使用 Navigation 组件实现页面之间的跳转、顶部导航栏动态切换、返回堆栈管理以及订单流程控制，从而完成一个完整的点餐导航流程。

------

## 二、核心知识点说明

### 1. NavController、NavHost 与 composable() 的关系

#### （1）NavController

`NavController` 是 Compose 导航的核心控制器，用于管理页面跳转、页面返回以及返回堆栈。

通过：

```kotlin
val navController = rememberNavController()
```

创建导航控制对象。

其主要作用包括：

- 控制页面导航；
- 管理返回栈；
- 实现页面返回；
- 监听页面状态变化。

------

#### （2）NavHost

`NavHost` 是页面导航的容器，用于承载所有可导航页面。

主要作用：

- 绑定 `NavController`；
- 设置起始页面；
- 配置所有页面路由。

示例：

```kotlin
NavHost(
    navController = navController,
    startDestination = LunchTrayScreen.Start.name
) {
    
}
```

------

#### （3）composable()

`composable()` 用于定义具体页面路由。

每个页面都对应一个独立的路由地址：

```kotlin
composable(route = LunchTrayScreen.Start.name) {
    StartOrderScreen()
}
```

------

### 三者之间的关系

整体执行流程如下：

```text
NavController → 控制导航
NavHost → 管理导航容器
composable() → 定义具体页面
```

当 `NavController` 发出导航命令后，`NavHost` 会根据 `composable()` 中定义的路由切换对应页面。

------

## 三、LunchTrayScreen 枚举类设计说明

本实验使用 `LunchTrayScreen` 枚举类统一管理页面路由与标题资源。

### 设计原因

1. 避免直接使用字符串路由；
2. 提高代码安全性；
3. 方便统一管理页面信息；
4. 提高代码可读性。

### 枚举类结构示例

```kotlin
enum class LunchTrayScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    EntreeMenu(title = R.string.choose_entree),
    SideDishMenu(title = R.string.choose_side_dish),
    AccompanimentMenu(title = R.string.choose_accompaniment),
    Checkout(title = R.string.order_checkout)
}
```

### 优点分析

| 优点     | 说明                 |
| -------- | -------------------- |
| 类型安全 | 避免字符串拼写错误   |
| 统一管理 | 页面信息集中维护     |
| 可读性高 | 页面含义更加明确     |
| 易扩展   | 后续新增页面更加方便 |

------

## 四、LunchTrayAppBar 设计思路

顶部导航栏主要实现了以下两个功能：

### 1. 动态标题

根据当前页面动态显示不同标题。

实现方式：

- 获取当前页面路由；
- 根据枚举类读取对应标题资源；
- 自动更新顶部栏标题。

------

### 2. 条件显示返回按钮

通过：

```kotlin
navController.previousBackStackEntry != null
```

判断当前是否存在上一级页面。

显示规则：

| 页面     | 是否显示返回按钮 |
| -------- | ---------------- |
| Start    | 否               |
| 其它页面 | 是               |

点击返回按钮后调用：

```kotlin
navController.navigateUp()
```

实现返回上一级页面。

------

## 五、导航流程与返回堆栈设计

### 1. 页面导航流程

应用整体导航流程如下：

```text
Start
  ↓
Entree
  ↓
SideDish
  ↓
Accompaniment
  ↓
Checkout
```

完成订单后返回 Start 页面。

------

### 2. 返回堆栈管理

返回堆栈管理是本实验的重要部分。

#### （1）开始点餐时清除 Start 页面

在 Start 页面进入点餐流程时：

```kotlin
popUpTo(LunchTrayScreen.Start.name) {
    inclusive = true
}
```

作用：

- 将 Start 页面从返回栈中移除；
- 用户在点餐流程中按返回键时直接退出应用；
- 更符合实际应用交互逻辑。

------

#### （2）Cancel 与 Submit 操作

点击 Cancel 或 Submit 时：

1. 返回 Start 页面；
2. 清空导航堆栈；
3. 重置订单数据。

------

#### （3）Next 页面跳转

普通 Next 操作仅进行页面跳转，不清除返回栈，从而支持逐级返回。

------

## 六、实验完成内容

本次实验主要完成了以下内容：

1. 创建 `LunchTrayScreen` 枚举类；
2. 初始化 `NavController`；
3. 配置 `NavHost` 页面导航；
4. 实现顶部栏动态标题；
5. 实现返回按钮逻辑；
6. 完成页面之间的跳转；
7. 实现取消订单与提交订单逻辑；
8. 完成返回堆栈管理；
9. 集成 ViewModel 实现订单状态同步。

------

## 七、实验过程中遇到的问题与解决方法

### 问题 1：findStartDestination() 报错

#### 原因

导航图尚未完全构建时调用该方法，导致编译异常。

#### 解决方法

直接使用：

```kotlin
LunchTrayScreen.Start.name
```

作为 `popUpTo()` 参数。

------

### 问题 2：返回键回到 Start 页面

#### 原因

未正确清除 Start 页面返回栈。

#### 解决方法

添加：

```kotlin
popUpTo(Start.name) {
    inclusive = true
}
```

将 Start 页面移出堆栈。

------

### 问题 3：Cancel 按钮代码重复

#### 原因

多个页面存在相同取消逻辑。

#### 解决方法

封装：

```kotlin
cancelAndNavigateToStart()
```

统一处理取消操作。

------

### 问题 4：顶部标题无法更新

#### 原因

页面变化后未监听导航状态。

#### 解决方法

使用：

```kotlin
currentBackStackEntryAsState()
```

实时监听当前页面变化。

------

## 八、实验验证结果

经过测试，应用运行结果符合实验要求：

1. 应用启动后默认显示 Start 页面；
2. Start 页面不显示返回按钮；
3. 点击 Start Order 能正确进入点餐流程；
4. 页面标题能够动态更新；
5. 支持逐级页面跳转与返回；
6. Cancel 可返回 Start 页面；
7. Submit 后订单能够正确重置；
8. 系统返回键逻辑正常；
9. 深色模式与浅色模式均可正常运行。

------

## 九、实验总结

通过本次实验，我掌握了 Jetpack Compose Navigation 的基本使用方法，理解了 `NavController`、`NavHost` 与 `composable()` 的协作机制。

同时，我进一步学习了：

- Compose 多页面导航实现；
- 返回堆栈管理；
- 动态顶部栏设计；
- 页面状态同步；
- Compose 项目结构规范。

本次实验让我对 Android 现代化开发架构有了更加深入的理解，也提高了我独立开发 Compose 多页面应用的能力。