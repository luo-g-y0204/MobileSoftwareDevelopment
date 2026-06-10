# Lab4：Dice Roller 交互应用与 Android Studio 调试实验报告

## 1. 应用界面结构说明

本次实验创建的 Dice Roller 应用采用了 Jetpack Compose 构建界面，主要包含以下组件：

- **Column**：作为根布局容器，设置了水平居中对齐和垂直居中排列
- **Image**：用于显示当前骰子点数的图片
- **Button**：用于触发掷骰子操作的按钮
- **Text**：按钮上显示的"Roll"文本

界面结构代码如下：
```kotlin
@Composable
fun DiceRollerApp() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DiceWithButtonAndImage()
    }
}
```

## 2. 使用 Compose 状态保存骰子结果

在 Compose 中，使用 `remember` 和 `mutableStateOf` 来保存界面状态：

```kotlin
var result by remember { mutableStateOf(1) }
```

- `remember` 确保状态在重组时得以保留
- `mutableStateOf` 创建一个可变状态对象
- `by` 关键字利用委托属性简化状态访问

当 `result` 的值发生变化时，Compose 会自动触发重组，更新界面显示。

## 3. 根据点数切换图片资源

使用 `when` 表达式将骰子点数映射到对应的图片资源：

```kotlin
val imageResource = when (result) {
    1 -> R.drawable.dice_1
    2 -> R.drawable.dice_2
    3 -> R.drawable.dice_3
    4 -> R.drawable.dice_4
    5 -> R.drawable.dice_5
    else -> R.drawable.dice_6
}
```

然后通过 `painterResource` 加载并显示图片：

```kotlin
Image(
    painter = painterResource(imageResource),
    contentDescription = result.toString()
)
```

## 4. 设置断点和观察内容

### 设置的断点位置

1. **断点1**：在 `MainActivity.onCreate()` 方法中调用 `setContent` 的位置
2. **断点2**：在 `DiceWithButtonAndImage()` 函数中 `val imageResource = when (result)` 赋值处

### 观察的内容

- 在第一个断点处观察应用启动过程
- 在第二个断点处观察每次点击按钮后 `result` 变量的值变化
- 在 Variables 窗格中查看状态变量的当前值

## 5. 调试功能使用体会

### Step Into

- **作用**：进入函数或方法内部执行
- **使用场景**：当需要查看函数内部实现细节时使用
- **体会**：通过 Step Into 可以深入了解 `setContent` 和各个 Composable 函数的执行过程

### Step Over

- **作用**：执行当前行代码，但不进入函数调用
- **使用场景**：当只需要执行当前语句，不需要了解函数内部时使用
- **体会**：适合快速浏览代码执行流程，了解整体逻辑

### Step Out

- **作用**：从当前函数返回到调用者
- **使用场景**：当已经了解了当前函数的主要逻辑，需要回到调用处继续执行时使用
- **体会**：可以快速退出函数，继续查看调用者的执行流程

## 6. 遇到的问题与解决过程

### 问题1：图片资源无法加载

**问题描述**：在运行应用时，出现资源找不到的错误。

**解决方法**：
1. 将骰子图片文件（dice_1.png 到 dice_6.png）复制到 `res/drawable/` 目录
2. 确保图片文件名与代码中引用的名称一致
3. 清理并重新构建项目

### 问题2：状态更新后界面没有刷新

**问题描述**：点击按钮后，状态变量值改变了，但界面没有更新。

**解决方法**：
1. 确保使用了 `mutableStateOf` 来保存状态
2. 使用 `by remember` 而不是直接使用 `remember`
3. 检查是否在正确的作用域内定义状态变量

### 问题3：调试时找不到状态变量

**问题描述**：在调试器中看不到 `result` 变量。

**解决方法**：
1. 状态变量在 Compose 中可能显示为 `result$delegate`
2. 展开 delegate 对象查看内部的 `value` 属性
3. 理解这是 Kotlin 委托属性的正常现象

## 7. 结论

### 为什么按钮点击后图片能够自动刷新

当用户点击按钮时，通过 `result = (1..6).random()` 更新了状态变量的值。由于 `result` 是使用 `mutableStateOf` 创建的状态，Compose 会检测到状态变化并自动触发重组，重新执行 `DiceWithButtonAndImage()` 函数，从而更新图片显示。

### 调试器中看到的变量值与界面结果是否一致

是的，调试器中观察到的 `result` 值与界面显示的骰子点数完全一致。当 `result` 的值从 1 变为其他数字时，界面上的骰子图片会相应地切换，证明了状态驱动界面更新的机制正常工作。

### 总结

通过本次实验，我掌握了：
1. 使用 Jetpack Compose 构建交互式界面的方法
2. 使用状态管理驱动界面更新的原理
3. Android Studio 调试器的基本使用方法
4. 理解了 Compose 中状态与界面之间的关系

实验成功完成，应用能够正常运行，点击按钮后骰子图片会随机变化。
