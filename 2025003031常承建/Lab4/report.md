# Lab4 实验报告

## 一、应用界面结构说明
我的应用使用 Jetpack Compose 编写，整体采用 Column 布局，实现屏幕居中显示。界面包含两个部分：一个用于显示骰子图片的 Image 组件，和一个用于点击掷骰子的 Button 组件。布局居中对齐，结构清晰。

## 二、使用 Compose 状态保存骰子结果
我在 DiceRollerApp 函数中使用以下代码保存骰子点数：
var result by remember { mutableStateOf(1) }
使用 remember 和 mutableStateOf 来保存状态，当 result 发生变化时，界面会自动重组刷新。

## 三、根据点数切换图片资源
我使用 when 表达式根据 result 的值切换对应的图片资源：
val imageResource = when (result) {
    1 -> java.com.example.diceroller.R.drawable.dice_1
    2 -> java.com.example.diceroller.R.drawable.dice_2
    3 -> java.com.example.diceroller.R.drawable.dice_3
    4 -> java.com.example.diceroller.R.drawable.dice_4
    5 -> java.com.example.diceroller.R.drawable.dice_5
    else -> java.com.example.diceroller.R.drawable.dice_6
}
并将结果传给 Image 组件显示。

## 四、断点设置与观察
我在 val imageResource = when (result) 这一行设置断点。程序暂停时，可以观察 result 的当前值，以及图片资源的切换过程。

## 五、Step Into、Step Over、Step Out 使用体会
Step Over 可以逐行执行代码，方便查看执行流程；
Step Into 可以进入函数内部，查看 Compose 函数的执行过程；
Step Out 可以快速跳出函数，回到上一层调用。

## 六、遇到的问题与解决过程
1. R 文件标红无法引用：使用完整包名引用资源解决。
2. 图片不显示：确保图片放在 drawable 文件夹且文件名正确。
3. 点击按钮不刷新：确认使用 remember + mutableStateOf 状态管理。

## 七、结论
按钮点击后图片自动刷新，是因为 Compose 的状态变化会触发界面重组。
调试器中看到的变量值与界面显示结果完全一致，实验完成。