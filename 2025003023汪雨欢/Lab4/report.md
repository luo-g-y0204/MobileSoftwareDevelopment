## 一、应用界面结构
采用 Kotlin + Jetpack Compose 开发，无 XML 布局。
主界面由 Column 实现居中布局，包含骰子图片、间距、Roll 按钮。
代码分为 MainActivity 入口、DiceRollerApp 主界面、DiceWithButtonAndImage 交互组件三层结构。

## 二、Compose 状态管理
使用 var result by remember { mutableStateOf(1) } 保存骰子点数。
remember 保证数据不丢失，mutableStateOf 使状态可观察，值变化自动触发界面重组。
点击按钮执行 result = (1..6).random() 更新点数，驱动界面刷新。

## 三、图片切换逻辑
通过 when 表达式将点数 1~6 映射到对应 dice_1 ~ dice_6 图片资源。
Image 组件使用动态资源，点数改变后图片自动切换，无需手动刷新。

## 四、调试操作
断点位置
DiceRollerApp() 调用处：观察界面初始化。
图片资源映射行：观察点数与图片匹配关系。
调试功能使用
Step Into：进入可组合函数查看内部执行。
Step Over：逐行执行代码，观察变量赋值。
Step Out：跳出函数，返回上层调用。
观察结果：result 变量随点击随机变化，与界面展示点数一致。

## 五、问题与解决
by 委托报错：补充 var/by 相关导入。
图片不更新：将写死图片改为动态资源变量。
语法报错：修正 Spacer 括号不匹配问题。
函数调用失败：删除多余的 name 参数。

## 六、实验结论
应用实现点击按钮随机掷骰子，图片随点数自动切换。
Compose 状态驱动界面刷新，状态变化→自动重组→界面更新。
调试可清晰观察变量变化与代码执行流程，理解 Compose 工作原理。