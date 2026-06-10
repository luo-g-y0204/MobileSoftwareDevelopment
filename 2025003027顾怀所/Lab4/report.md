# Lab4：Dice Roller 交互应用与 Android Studio 调试

## 一、应用界面结构说明
本次实验使用 Jetpack Compose 构建掷骰子应用，整体界面结构如下：
1. 最外层使用 Column 布局，让内容在屏幕中居中显示。
2. 核心组件为 DiceWithButtonAndImage，包含图片与按钮。
3. 界面分为两部分：骰子图片展示区、Roll 按钮区。
4. 使用 Spacer 控制图片与按钮之间的间距，使布局更美观。

## 二、Compose 状态保存骰子结果
使用 Compose 的状态管理机制保存骰子点数：
- 使用 var result by remember { mutableStateOf(1) } 创建可观察状态。
- remember 保证配置变化时数据不丢失。
- mutableStateOf 使状态变化时自动触发界面重组。
- 点击按钮时更新 result = (1..6).random()，驱动界面刷新。

## 三、根据点数切换图片资源
通过 when 表达式将点数映射到对应图片：
1. 点数 1~6 分别对应 dice_1 到 dice_6 图片。
2. 状态 result 变化时，imageResource 自动更新。
3. Image 组件根据新资源自动刷新显示。

## 四、断点设置与调试观察
1. 断点位置：
   - DiceWithButtonAndImage 中 imageResource 赋值行
   - 按钮 onClick 点击事件内部
2. 观察内容：
   - 点击按钮时 result 变量的变化
   - imageResource 随点数切换对应的图片资源
   - Compose 重组时变量值的更新过程

## 五、调试功能使用体会
1. Step Into：进入函数内部，可查看 random() 执行过程。
2. Step Over：逐行执行代码，观察变量一步步变化。
3. Step Out：跳出当前函数，回到调用处。
通过调试能清晰看到状态变量如何驱动界面刷新。

## 六、遇到的问题与解决过程
1. 问题：随机数语法错误，使用了错误符号 ≤..≤。
   解决：改为 Kotlin 标准写法 (1..6).random()。
2. 问题：mutableStateOf(value:1) 语法错误。
   解决：去掉多余的 value:，改为 mutableStateOf(1)。
3. 问题：界面不居中。
   解决：使用 wrapContentSize(Alignment.Center) 实现居中。

## 七、实验结论
1. 按钮点击后图片自动刷新，是因为 Compose 状态驱动重组。
2. 调试器中变量值与界面显示完全一致。
3. Compose 的状态管理简化了界面更新逻辑，无需手动刷新视图。

