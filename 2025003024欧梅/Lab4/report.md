# Lab4：Dice Roller 交互应用与 Android Studio 调试实验报告
姓名：欧梅
学号：2025003024

---

## 一、应用界面结构说明
本次实验使用 Kotlin + Jetpack Compose 完成 Dice Roller 掷骰子交互应用，界面采用纯 Compose 实现，不使用 XML 布局。

界面整体结构：
- MainActivity：应用程序入口，负责加载 Compose 界面
- DiceRollerApp：根可组合函数，使用 Column 实现整体居中布局
- DiceWithButtonAndImage：核心交互组件，包含图片显示与按钮

布局方式：
- 使用 Modifier.fillMaxSize() 占满屏幕
- horizontalAlignment = Alignment.CenterHorizontally 实现水平居中
- verticalArrangement = Arrangement.Center 实现垂直居中
使骰子图片与按钮在屏幕中央显示。

---

## 二、Compose 状态保存骰子结果
使用 Compose 状态管理机制保存当前骰子点数：

var result = remember { mutableIntStateOf(1) }

- remember：保证数据在界面重组时不会丢失
- mutableIntStateOf：创建可观察的整型状态，初始值为 1
- result.intValue：获取或修改当前点数

当 result.intValue 发生变化时，Compose 会自动重组界面，刷新图片。

---

## 三、根据点数切换图片资源
通过 when 表达式将骰子点数映射到对应的图片资源：

val imageResource = when (result.intValue) {
    1 -> R.drawable.dice_1
    2 -> R.drawable.dice_2
    3 -> R.drawable.dice_3
    4 -> R.drawable.dice_4
    5 -> R.drawable.dice_5
    else -> R.drawable.dice_6
}

Image 组件通过 painterResource(imageResource) 动态加载对应图片，实现图片随点数自动切换。
contentDescription 设置为当前点数文字描述，提升应用无障碍性。

---

## 四、断点设置与观察内容
本次实验设置两个关键断点，位置与观察内容如下：
1. **断点1：MainActivity 中 DiceRollerApp() 调用处（第38行）**
   作用：观察应用启动流程，验证主界面组件的加载入口。
2. **断点2：val imageResource = when (result.intValue) 赋值语句处（第60行）**
   作用：观察骰子点数变化与图片资源的对应关系，验证状态驱动UI的逻辑。

调试时可观察到：
- 程序启动后停在断点1，确认主界面函数正常调用
- 点击 Roll 按钮后，result.intValue 生成1~6随机数，状态更新触发界面重组
- 程序停在断点2，imageResource 根据新点数重新计算对应图片资源
- 继续运行后，界面自动刷新为新点数对应的骰子图片

---

## 五、调试功能使用体会
1. Step Into：进入函数内部，查看代码执行流程
2. Step Over：逐行执行，观察变量变化
3. Step Out：跳出当前函数，回到上层调用逻辑

通过调试，我理解了 Compose 状态驱动 UI 的工作原理：
状态变化 → 重组界面 → UI 自动更新。

---

## 六、遇到的问题与解决过程
1. 图片资源无法找到
   原因：未将 dice_1 至 dice_6 矢量图放入 drawable 文件夹
   解决：将图片导入后同步项目，资源正常加载。

2. 界面布局不居中
   原因：缺少垂直居中配置
   解决：添加 verticalArrangement = Arrangement.Center 后与示例效果一致。

3. 状态无法正确更新
   原因：状态声明方式不规范
   解决：使用 remember + mutableIntStateOf 标准写法，状态可正常更新。

4. 按钮文字样式不统一
   解决：使用 MaterialTheme.typography.labelLarge 统一按钮文字风格。

5. 调试时界面不显示
   原因：程序停在断点处未继续执行
   解决：点击 Resume Program 继续运行，界面正常加载。

---

## 七、实验结论
1. 应用实现点击按钮随机掷骰子功能，图片可正确切换。
2. 使用 remember + mutableIntStateOf 实现 Compose 状态管理。
3. 通过 when 表达式完成点数到图片资源的映射。
4. 调试过程验证了状态变化驱动界面刷新的机制。
5. 界面布局、样式、功能均与实验要求一致，可正常运行与交互。