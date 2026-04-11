# Lab4：Dice Roller 交互应用与 Android Studio 调试 实验报告
## 一、应用界面结构说明
本应用基于 **Kotlin + Jetpack Compose** 实现，完全不使用 XML 布局，界面结构分层清晰：
1. **入口层**：`MainActivity` 作为应用启动入口，在 `onCreate` 方法中通过 `setContent` 加载 Compose 界面；
2. **应用主组件**：`DicerollerApp` 是顶层可组合函数，仅负责调用核心功能组件；
3. **核心功能组件**：`DiceWithButtonAndImage` 实现完整交互逻辑，采用 **Column 垂直布局**，内部包含：
   - 显示骰子的 `Image` 组件（居中展示）；
   - 16dp 的 `Spacer` 间隔组件；
   - 触发掷骰子的 `Button` 按钮；
4. **布局特性**：通过 `Modifier` 实现界面全屏、居中对齐，整体界面简洁且交互直观。

## 二、Compose 状态保存骰子结果的实现
应用通过 **Jetpack Compose 状态管理** 保存骰子点数，核心代码：
```kotlin
var result by remember { mutableStateOf(1) }
```
1. `mutableStateOf(1)`：创建一个**可观察的状态变量**，初始值为1（默认骰子点数）；
2. `remember`：将状态变量缓存，界面重组时不会重新初始化，保证点数数据不丢失；
3. `by` 委托：简化状态变量的读写，直接通过 `result` 赋值/取值；
当 `result` 的值发生变化时，Compose 会自动触发相关组件**重组**，驱动界面刷新。

## 三、根据点数切换图片资源的逻辑
通过 **`when` 表达式** 实现骰子点数与图片资源的映射，核心代码：
```kotlin
val imageResource = when(result){
    1 -> R.drawable.dice_1
    2 -> R.drawable.dice_2
    3 -> R.drawable.dice_3
    4 -> R.drawable.dice_4
    5 -> R.drawable.dice_5
    else -> R.drawable.dice_6
}
```
1. 监听状态变量 `result` 的值（1~6的随机数）；
2. 根据不同点数，匹配 `res/drawable` 目录下对应的骰子图片资源；
3. 将匹配后的资源 ID 传入 `Image` 组件的 `painterResource`，完成图片展示；
状态变化时，`imageResource` 会自动更新，`Image` 组件同步刷新显示。

## 四、断点设置与观察内容
本次实验设置**两个关键断点**，满足调试要求：
1. **断点1**：`MainActivity` 的 `onCreate` 中 `DicerollerApp(` 调用行
   - 观察内容：应用启动时的界面加载流程，查看函数调用栈和初始参数；
2. **断点2**：`DiceWithButtonAndImage` 中 `val imageResource=when(result){` 行
   - 观察内容：骰子点数 `result` 的实时值、图片资源 `imageResource` 的映射结果，验证状态更新逻辑。

## 五、调试功能（Step Into/Step Over/Step Out）使用体会
1. **Step Into（步入）**：点击后进入当前行调用的函数内部，用于深入查看子函数（如 `DicerollerApp`、`DiceWithButtonAndImage`）的执行逻辑，适合追踪界面组件的加载流程；
2. **Step Over（步过）**：逐行执行当前函数代码，不进入子函数内部，效率更高，适合快速查看当前函数的变量变化和执行顺序；
3. **Step Out（步出）**：跳出当前函数，返回上一层调用位置，适合快速退出子函数，回到主流程。
通过三个调试功能，能清晰追踪代码执行链路，理解 Compose 界面的加载和重组流程。

## 六、遇到的问题与解决过程
1. **问题1**：点击按钮后骰子图片不刷新
   - 原因：未使用 Compose 状态管理，直接用普通变量存储点数，界面无法感知变化；
   - 解决：使用 `remember + mutableStateOf` 定义状态变量 `result`，通过状态驱动界面重组。
2. **问题2**：断点设置后程序不暂停
   - 原因：使用普通运行模式启动应用，而非调试模式；
   - 解决：点击 Android Studio 顶部「Debug 'app'」图标，以调试模式启动应用。
3. **问题3**：初始状态骰子点数丢失
   - 原因：未使用 `remember` 缓存状态，界面重组时变量重新初始化；
   - 解决：给状态变量添加 `remember` 包裹，保证数据持久化。

## 七、实验结论
1. **按钮点击后图片自动刷新的原因**：
   点击按钮时，随机数更新 Compose 状态变量 `result`，状态变化触发 Composable 函数**自动重组**，`when` 表达式重新匹配图片资源，`Image` 组件同步刷新，实现图片自动更新。
2. **调试变量与界面结果一致性**：
   调试器中观察到的 `result` 变量值，与界面展示的骰子图片完全匹配；变量更新后，界面图片立即同步变化，验证了 Compose **状态驱动界面**的核心特性。
3. **调试效果**：通过断点和调试功能，清晰观察到代码执行流程和状态变量变化，掌握了 Android Studio 基础调试技能。