# Lab4：Dice Roller 交互应用与 Android Studio 调试 实验报告

## 一、应用界面结构说明
本项目使用 Jetpack Compose 的 `Column` 布局，将 `Image`（显示骰子）和 `Button`（点击按钮）垂直居中排列。通过 `mutableStateOf` 管理骰子点数状态，当状态变化时自动重组刷新 UI。

## 二、Compose 状态保存骰子结果的实现
定义 `var result by remember { mutableStateOf(1) }`，使用 `remember` 记住状态，`mutableStateOf` 监听数值变化，触发布局重组，实现状态驱动的界面更新。

## 三、根据点数切换图片资源
将 6 张骰子图片放入 `res/drawable` 目录，通过 `when(result)` 表达式匹配 1-6，返回对应的 `R.drawable.dice_x` 资源 ID，绑定点数与图片。

## 四、断点设置与调试观察
1.  在 `DiceRollerApp` 函数入口设断点：观察初始状态，`result` 为 1，加载默认骰子。
2.  在 `Button` 的 `onClick` 设断点：点击 Roll 后，观察 `result` 被赋值为 1-6 的随机数。
3.  在 `imageResource` 赋值处设断点：观察 `when` 表达式如何映射点数和图片资源。

## 五、Step Into / Step Over / Step Out 的使用体会
- `Step Over`：逐行执行代码，跳过函数细节，快速跟踪流程。
- `Step Into`：进入 `DiceRollerApp` 内部，查看 Composable 的具体执行步骤。
- `Step Out`：跳出当前函数，返回上层调用，适合跳出深层循环。

## 六、遇到的问题与解决过程
运行时遇到 `Broken pipe` 安装失败，解决方法：使用 Device Manager 的 `Wipe Data` 清空模拟器数据，重新运行，问题解决。

## 七、结论
1.  按钮点击后图片自动刷新的原理：Jetpack Compose 通过状态驱动界面重组，状态变化时自动触发界面更新，无需手动刷新。
2.  调试器变量值与界面结果一致性：调试器中 `result` 变量的数值，与界面显示的骰子点数完全一致，验证了代码逻辑的正确性。