# Lab4：Dice Roller 交互应用与 Android Studio 调试


## 一、应用界面结构说明
我的 Dice Roller 应用完全使用 Jetpack Compose 声明式 UI 构建，整体界面结构清晰、层次分明：

- MainActivity：应用入口，负责初始化界面、启用沉浸式布局，并在 setContent 中加载主题和主界面组件。
- DiceRollerApp()：应用顶层容器组件，作为统一入口调用核心功能组件。
- DiceWithButtonAndImage()：核心交互组件，使用 Column 垂直布局，包含：
  - 居中显示的骰子 Image
  - 与图片之间保留间距的 Spacer
  - 触发掷骰子操作的 Button

所有元素通过 Modifier 实现全屏居中对齐，界面简洁、交互直观。

## 二、使用 Compose 状态保存骰子结果
我使用 remember + mutableStateOf 组合实现 Compose 状态管理，保存骰子点数：

var result by remember { mutableStateOf(1) }

- mutableStateOf(1)：创建一个可观察的状态变量，初始点数为 1。
- remember：在界面重组时保留变量值，不会被重复初始化。
- 当 result 被修改时，Compose 会自动触发重组（Recomposition），刷新界面。

这种方式实现了状态驱动 UI，是本次应用可以自动刷新的核心。

## 三、根据点数切换图片资源
我使用 when 表达式根据点数映射对应的图片资源。
每当状态 result 发生变化，imageResource 会自动重新计算，Image 组件通过 painterResource(imageResource) 自动加载最新图片，实现图片动态切换。

## 四、断点设置与观察内容
我在代码中设置了 2 个关键断点：

断点 1:
位置：val imageResource = when (result)
观察：result 点数是否正确、imageResource 是否匹配到正确图片 ID。

断点 2:
位置：Button 的 onClick 事件内部 result = (1..6).random()
观察：点击按钮时随机数是否生成、result 变量是否被正确赋值。

通过断点，我清晰观察到：
- 应用启动时 result = 1
- 点击按钮后 result 变为 1~6 随机数
- 图片资源随数值同步更新

## 五、Step Into / Step Over / Step Out 使用体会
Step Over（单步跳过）
最常用，逐行执行当前代码，不进入函数内部。适合观察变量赋值、逻辑判断流程，我用它查看 result 赋值过程。

Step Into（单步跳入）
进入代码调用的函数内部。我用它进入 mutableStateOf() 了解 Compose 状态创建逻辑。

Step Out（单步跳出）
从当前函数跳出，回到上层调用处。用于快速退出系统函数，回到自己的业务代码，提升调试效率。

三者配合可以完整追踪代码执行路线与变量变化。

## 六、遇到的问题与解决过程
问题：界面重组后点数重置
原因：未使用 remember 包裹状态
解决：使用 remember { mutableStateOf(1) } 保存状态。

问题：预览界面不显示内容
原因：Preview 中没有调用 DiceRollerApp()
解决：在预览函数中添加组件调用。

问题：点击按钮无反应
原因：随机数范围写错
解决：改为 (1..6).random()。

## 七、实验结论
按钮点击后图片自动刷新的原因
点击按钮修改 result 状态变量 → Compose 检测到状态变化 → 触发组件重组 → 重新计算图片资源 → 自动更新 Image 组件。全程无需手动刷新UI，完全由状态驱动。

调试变量与界面结果一致
调试器中观察到的 result 值、imageResource 资源 ID，与界面显示图片完全一致，验证了 Compose 状态管理的可靠性。

总结
Jetpack Compose 大幅简化了交互式界面开发，状态管理简洁高效；Android Studio 调试器可以清晰追踪代码执行与变量变化，帮助快速定位问题。