# Lunch Tray 点餐应用导航实验报告

## 1. Compose Navigation 中 NavController、NavHost 和 composable() 三者之间的关系简述

在 Jetpack Compose 中，Navigation 组件通过三个核心要素协同工作，实现页面导航：

- **NavController**：导航控制器，是整个导航系统的“大脑”。它维护着一个返回堆栈（back stack），记录了用户访问过的页面顺序。`NavController` 提供了 `navigate()` 用于跳转到新页面，`navigateUp()`用于返回上一页，`popBackStack()` 用于手动弹出栈顶页面。通过 `rememberNavController()` 创建，并在整个 `NavHost` 生命周期内保持。
- **NavHost**：导航宿主容器，它定义了一个导航图（NavGraph），并指定起始目的地（startDestination）。`NavHost` 会监听 `NavController` 的当前状态，根据当前路由（route）渲染对应的 Composable 页面。它相当于一个“路由器”，负责根据路由选择显示哪个页面。
- **composable()**：`NavGraphBuilder` 的扩展函数，用于将一条路由（字符串）与一个可组合函数绑定。每当 `NavController` 导航到该路由时，`NavHost` 就会调用该 `composable` 内的 UI 构建代码。每个 `composable()` 通常对应一个屏幕。

**三者协作流程**：开发者在 `NavHost` 体内调用多个 `composable()`，注册路由与页面的映射关系；用户交互触发 `navController.navigate(route)`；`NavHost` 监听到当前路由变化后，找到匹配的 `composable`，并重组显示对应页面。同时，`NavController` 自动维护返回栈，支持系统返回键和手动回退。

## 2. LunchTrayScreen 枚举类的设计说明

本实验中，我们定义了一个枚举类 `LunchTrayScreen`，用于统一管理所有页面的路由名称和标题：

kotlin

```
enum class LunchTrayScreen(@StringRes val title: Int) {
    Start(R.string.app_name),
    Entree(R.string.choose_entree),
    SideDish(R.string.choose_side_dish),
    Accompaniment(R.string.choose_accompaniment),
    Checkout(R.string.order_checkout)
}
```



**使用枚举而非字符串的原因**：

1. **类型安全**：枚举值在编译期即确定，避免了字符串拼写错误或大小写不一致导致的路由无法匹配问题。例如，`LunchTrayScreen.Entree.name` 总能得到正确的 `"Entree"`。
2. **关联额外信息**：枚举可以携带属性，这里每个枚举值关联一个字符串资源 ID。这使得 AppBar 可以方便地通过 `stringResource(currentScreen.title)` 动态获取标题，无需维护额外的映射表。
3. **集中管理**：所有页面的路由和标题集中在一个地方定义，修改或增加页面时只需改动枚举类，提高可维护性。
4. **易于反向查找**：通过 `LunchTrayScreen.valueOf(route)` 可以从路由字符串反向获取枚举对象，从而获得标题等元数据。这在根据当前路由动态更新 AppBar 时非常有用。
5. **代码可读性**：使用枚举比散落的字符串常量更具表现力，让代码意图更清晰。

## 3. LunchTrayAppBar 的设计思路

`LunchTrayAppBar` 是一个可复用的顶部应用栏组件，其设计遵循以下思路：

kotlin

```
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayAppBar(
    currentScreen: LunchTrayScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) { ... }
```



### 功能要点

- **动态标题**：通过 `currentScreen.title` 获取当前页面对应的字符串资源 ID，再利用 `stringResource()` 转换为实际文本，实现标题随页面变化自动更新。
- **返回按钮**：使用 `Icons.Filled.ArrowBack` 作为图标，点击时执行传入的 `navigateUp` 回调（实际调用 `navController.navigateUp()`）。
- **返回按钮显示条件**：通过 `canNavigateBack` 参数控制。该参数在 `LunchTrayApp` 中通过 `navController.previousBackStackEntry != null` 计算得出。当返回栈中存在上一页时显示返回按钮，否则不显示。因为 Start 页面没有上一页，所以不会显示返回按钮，符合 Material Design 规范。

### 设计优点

- **解耦**：`LunchTrayAppBar` 不直接依赖 `NavController`，而是接收布尔值和回调函数，便于单独测试和预览。
- **符合单一职责**：AppBar 只负责显示标题和返回按钮，不关心导航逻辑。
- **主题适配**：使用 `MaterialTheme.colorScheme.primaryContainer` 作为背景色，自动适配浅色/深色模式。

## 4. 导航流程的设计说明及返回堆栈管理

### 导航流程总览

- Start → Entree → SideDish → Accompaniment → Checkout → Start（提交订单后）
- 任意页面点击 Cancel → Start

### 返回堆栈管理的关键点

#### 为什么 Start 页面需要被弹出？

当用户从 Start 页面点击 “Start Order” 进入主菜页面时，我们通过以下代码实现跳转：

kotlin

```
navController.navigate(LunchTrayScreen.Entree.name) {
    popUpTo(LunchTrayScreen.Start.name) { inclusive = true }
}
```



- `popUpTo(LunchTrayScreen.Start.name)` 指示导航系统将返回栈中所有位于 Start 页面之上的页面弹出（此时还没有，但将来也不会保留 Start）。
- `inclusive = true` 表示同时移除 Start 页面本身。

**效果**：Start 页面不再存在于返回栈中。此时用户按系统返回键，由于栈底不是 Start，而是直接退出应用。这符合用户预期：点餐是一个独立流程，完成后不应再回到起始页；从起始页开始点餐后，按返回键应取消整个流程（退出应用），而不是回到无意义的 Start 页面。

#### Cancel 和 Submit 操作的处理

任意页面点击 “Cancel” 或结账页面点击 “Submit” 后，需要回到 Start 并清空整个返回栈，让用户可以重新开始一次全新的点餐。

代码实现：

kotlin

```
navController.navigate(LunchTrayScreen.Start.name) {
    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
}
```



- `findStartDestination().id` 获取导航图起始目的地的 ID（即 Start 页面）。
- 清空所有页面后，将 Start 作为唯一页面放入栈中。

同时需要调用 `viewModel.resetOrder()` 重置订单数据，避免旧数据残留。

### 正常导航（Next）

正常情况下，各页面之间的 Next 按钮只执行简单的 `navigate()`，不额外处理返回栈，因为用户希望通过系统返回键逐级回退（例如从配菜返回主菜修改选择），这是合理的预期行为。

## 5. 实验中遇到的问题与解决过程

### 问题一：枚举路由解析错误

**现象**：在 `currentScreen` 计算中使用 `LunchTrayScreen.valueOf(backStackEntry?.destination?.route ?: ...)` 时，有时会抛出 `IllegalArgumentException`，原因是某些页面的路由可能携带查询参数（如 `"Entree?someParam=value"`），导致 `valueOf` 无法匹配。

**解决**：由于本实验所有页面均无需传递参数，路由就是简单的枚举名称，不会出现参数后缀。但为了代码健壮性，可以增加 `substringBefore("?")` 处理。最终确认起始代码中所有 `composable` 路由未带参数，因此直接使用原方案没有问题。

### 问题二：订单数据残留

**现象**：提交订单后回到 Start，再重新点餐，发现之前选择的主菜、配菜等依然显示在菜单页面上。

**原因**：虽然导航回到了 Start，但 ViewModel 中的订单状态没有被重置。

**解决**：在所有 Cancel 回调以及 Checkout 页面的 Submit 回调中，显式调用 `viewModel.resetOrder()`。同时确保在 `StartOrderScreen` 进入点餐流程时不需要重置（因为此时应该是空订单）。修改后，每次开始新点餐都会得到干净的订单状态。

### 问题三：深色模式下 AppBar 颜色对比度不足

**现象**：使用默认的 `TopAppBarDefaults.mediumTopAppBarColors()` 在深色主题下，背景色与文字色对比度较低，标题不易阅读。

**解决**：将容器颜色显式设为 `MaterialTheme.colorScheme.primaryContainer`。该颜色在浅色和深色主题下均由 MaterialTheme 自动提供合适的色值，确保可读性。同时保留内容颜色为默认值（自动适应）。

### 问题四：系统返回键与 Cancel 按钮的行为差异

**分析**：实验要求中并未要求修改系统返回键的行为。用户点击 Cancel 按钮会清空栈回到 Start 并重置订单；而系统返回键则按正常栈顺序回退（例如从配菜返回到主菜）。这种差异是合理的设计：Cancel 表示“取消整个订单”，返回键表示“回到上一步修改选择”。无需额外处理，最终用户体验良好。

### 问题五：Preview 功能失效

**现象**：在 `LunchTrayApp` 的 Preview 中，由于缺少真实的 `NavController` 和 ViewModel，预览无法正常显示。

**解决**：本实验不要求提供 Preview，或者可以在 Preview 中传入 mock 状态。提交代码时保留原样即可，不影响运行。

## 总结

通过本次实验，我掌握了 Jetpack Compose Navigation 的核心用法：使用枚举定义路由、创建 NavController 和 NavHost、配置 composable 页面、管理返回堆栈以优化用户体验。同时深入理解了如何通过 `popUpTo` 和 `inclusive` 控制返回栈行为，确保导航流程符合设计预期。实验过程中遇到的状态残留和主题适配问题，也加深了对 Compose 状态管理和 Material Design 主题机制的理解。
