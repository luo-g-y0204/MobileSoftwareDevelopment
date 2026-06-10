# Lab4 实验报告
## 一、实验信息
- 实验名称：Dice Roller 交互应用与 Android Studio 调试
- 姓名：郎梦婷
- 学号：2025003006

## 二、应用界面结构说明
应用使用 Jetpack Compose 构建，整体采用 Column 垂直布局，实现界面居中显示。
界面包含：
1. 显示骰子图片的 Image 组件
2. 触发掷骰子操作的 Button 组件
3. 使用 Column 作为根容器，实现内容垂直居中

## 三、Compose 状态使用说明
使用 remember + mutableStateOf 保存骰子点数：
var result by remember { mutableStateOf(1) }
- remember：保证状态在界面重组时不丢失
- mutableStateOf：创建可观察状态，值变化时自动刷新界面

点击按钮时，result 被更新为 1~6 的随机数，触发界面重组。

## 四、图片资源切换逻辑
使用 when 表达式根据点数映射图片：
val imageResource = when (result) {
    1 -> R.drawable.dice_1
    2 -> R.drawable.dice_2
    3 -> R.drawable.dice_3
    4 -> R.drawable.dice_4
    5 -> R.drawable.dice_5
    else -> R.drawable.dice_6
}
每次 result 变化，图片会自动更新。

## 五、调试过程
断点位置：
1. val imageResource = when(result) 处
2. 按钮点击事件处

调试操作：
- Step Into：进入函数内部
- Step Over：逐行执行
- Step Out：跳出当前函数

在调试窗口中可以观察 result 变量实时变化，与界面显示一致。

## 六、调试操作体会
- Step Over 适合顺序查看代码执行流程
- Step Into 可以深入函数理解逻辑
- Step Out 快速返回上层调用
通过调试能清晰看到状态更新 → 界面刷新的完整流程。

## 七、遇到的问题与解决
1. 图片不更新：未使用状态变量，改为 mutableStateOf 后解决
2. 资源找不到：图片文件名不规范，统一命名后解决
3. 断点不生效：使用 Debug 模式启动后正常

## 八、实验结论
本次实验成功实现掷骰子交互应用，掌握 Compose 状态管理与 Android Studio 调试方法。
状态驱动 UI 是 Compose 的核心机制，变量变化自动触发界面刷新，代码简洁高效。