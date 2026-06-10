# Lab4：Dice Roller 实验报告

## 一、应用界面结构说明

本应用采用单页面设计，界面结构如下：

- **主容器**：使用 `Column` 组件实现垂直布局
- **图片显示**：`Image` 组件居中显示骰子图片
- **按钮**：`Button` 组件用于触发掷骰子操作

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

## 二、Compose 状态保存骰子结果

使用 `remember` 和 `mutableStateOf` 保存骰子点数：

```kotlin
var result by remember { mutableStateOf(1) }
```

当 `result` 的值发生变化时，Compose 会自动触发 UI 重组（recomposition），界面会更新为新的骰子图片。

## 三、根据点数切换图片资源

使用 `when` 表达式将点数映射到不同的图片资源：

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

## 四、断点设置与调试过程

### 断点位置

1. **主断点1**：`onCreate()` 中调用 `DiceRollerApp()` 的位置（第33行）
2. **主断点2**：`imageResource` 赋值的位置（第42行）

### 调试过程

1. 在上述两个位置设置断点
2. 使用 **Debug 'app'** 模式启动应用
3. 程序在断点处暂停，观察变量值

## 五、调试功能使用体会

### Step Into (F7)
进入函数调用内部查看具体实现，例如进入 `DiceRollerApp()` 函数。

### Step Over (F8)
逐行执行代码，观察每行代码执行后变量的变化。

### Step Out (Shift+F8)
从当前函数返回到调用位置，快速跳过已理解的代码段。

## 六、遇到的问题与解决

### 问题：程序无法编译
**原因**：变量名拼写错误 `imageResourse`
**解决**：修正为 `imageResource`

### 问题：图片不显示
**原因**：drawable 资源未添加到项目中
**解决**：将 dice_1.xml ~ dice_6.xml 复制到 `res/drawable/` 目录

## 七、结论

### 为什么按钮点击后图片能自动刷新？
因为使用了 Compose 状态管理。当 `result` 变量通过 `mutableStateOf` 声明后，任何对这个变量的修改都会触发重组（recomposition），Compose 会自动重新执行相关 Composable 函数，从而更新界面显示。

### 调试器中看到的变量值与界面结果是否一致？
是的。在调试过程中，`result` 变量的值与界面上显示的骰子点数保持一致，验证了 Compose 状态驱动的正确性。
