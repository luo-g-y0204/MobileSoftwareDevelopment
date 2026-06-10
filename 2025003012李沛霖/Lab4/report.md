# Lab4 实验报告
1. 界面结构
使用 Column 布局，整体居中，内部包含骰子图片 Image 和掷骰子按钮 Button。

2. 状态管理
使用 remember + mutableStateOf 保存骰子点数 result，状态变化时自动触发界面重组。

3. 图片切换
使用 when 表达式，根据 result 的值 1~6 分别对应 dice_1 到 dice_6 矢量图资源。

4. 断点设置
在图片资源赋值处设置断点，点击按钮后可观察 result 变量变化。

5. 调试操作体会
Step Over：逐行执行代码。
Step Into：进入函数内部。
Step Out：跳出当前函数。
能清楚看到状态变量改变并驱动界面刷新。

6. 遇到的问题与解决
- 图片不显示：检查矢量图是否放在 drawable 文件夹，文件名是否正确。
- 代码报错：替换完整 MainActivity 代码后解决。

结论：Compose 通过状态监听实现自动界面刷新，调试器可直观观察变量变化。