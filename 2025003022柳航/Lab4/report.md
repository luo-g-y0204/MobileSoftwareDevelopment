# 移动软件开发实验报告
班级：计算机科学与技术班
学号：2025003022
姓名：柳航
实验日期：2025年4月10日
实验名称：DiceRoller 掷骰子应用开发

## 一、实验目的
1.  掌握 Android 开发中图片资源（drawable）的导入方法及命名规范，能正确导入和引用资源文件。
2.  熟练使用 Jetpack Compose 构建 UI 界面，理解声明式 UI 的核心思想和基本用法。
3.  掌握 Compose 状态管理的基础操作，实现数据变化与 UI 同步更新。
4.  学会使用 Android Studio 调试工具，能熟练执行 Step Into、Step Out、Step Over 等调试操作。
5.  完成项目开发、调试、报告编写、Git 提交的全流程，符合课程作业规范。

## 二、实验环境
- 操作系统：Windows 11
- 开发工具：Android Studio（Hedgehog 版本）
- 模拟器：Pixel 8（API 34）
- 开发语言：Kotlin
- 开发框架：Jetpack Compose
- 版本控制：Git
- 资源文件：骰子图片资源（dice_1 至 dice_6）

## 三、实验内容与步骤
### （一）项目创建
1.  打开 Android Studio，新建 Empty Activity 项目，项目名称为 DiceRoller，语言选择 Kotlin，勾选 Use Compose。
2.  配置项目结构，确保 res 文件夹下存在 drawable 文件夹，用于存放骰子图片资源。

### （二）资源导入
1.  将提前准备好的 6 个骰子图片资源（dice_1.xml 至 dice_6.xml），直接复制到 res/drawable 文件夹中。
2.  检查资源文件名，确保均为小写、下划线命名（无大写、无空格、无特殊字符），避免资源引用失败。

### （三）界面开发与功能实现
1.  使用 Jetpack Compose 的 Column 布局，实现 UI 垂直居中，包含骰子图片和 Roll 按钮。
2.  定义状态变量，用于记录当前骰子点数，实现点击按钮时随机生成点数，并切换对应骰子图片。
3.  配置按钮点击事件，调用随机数生成方法，实现 UI 随数据变化自动刷新。

### （四）调试操作
1.  在 MainActivity 的随机数赋值代码行添加断点，用于调试。
2.  执行 Step Into 操作，进入随机数方法内部，观察调用栈变化。
3.  执行 Step Out 操作，退出方法，回到主代码逻辑，完成调试流程。

### （五）报告编写与提交
1.  整理调试过程中的截图，按要求命名（与实验步骤对应）。
2.  编写实验报告，按规范填写实验目的、环境、步骤、结果等内容。
3.  通过 Git 提交项目、报告及截图，完成作业提交。

## 四、核心代码实现
```kotlin
package com.example.diceroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.diceroller.ui.theme.DiceRollerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiceRollerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiceRollerApp()
                }
            }
        }
    }
}

@Composable
fun DiceRollerApp() {
    // 状态变量：记录当前骰子点数，初始值为1
    var diceResult by remember { mutableStateOf(1) }

    // 根据点数匹配对应的骰子图片资源
    val diceImage = when (diceResult) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }

    // 垂直居中布局，包含骰子图片和按钮
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 显示骰子图片
        Image(
            painter = painterResource(id = diceImage),
            contentDescription = "骰子点数：$diceResult"
        )
        // 间距设置
        Spacer(modifier = Modifier.height(32.dp))
        // 掷骰子按钮
        Button(onClick = {
            // 生成1-6的随机数
            diceResult = (1..6).random()
        }) {
            Text(text = stringResource(id = R.string.roll))
        }
    }
}

// UI预览
@Preview(showBackground = true)
@Composable
fun DiceRollerPreview() {
    DiceRollerTheme {
        DiceRollerApp()
    }
}
五、运行结果
应用启动成功，界面显示骰子图片与 Roll 按钮，布局居中、显示正常。点击 Roll 按钮，骰子图片随机切换为 1~6 点，功能正常、无报错、无卡顿。运行界面已截图保存，符合实验要求。

六、调试过程
Step Over（F8）：单步执行代码，查看变量 diceResult 的值变化。
Step Into（F7）：进入 random() 方法内部，查看系统源码实现。
Step Out（Shift+F8）：退出方法，回到主程序逻辑，继续执行后续代码。
调试过程全部完成，截图清晰完整，满足实验要求。

七、实验总结
本次实验完成了 DiceRoller 掷骰子应用的开发，掌握了 Android 资源导入、Jetpack Compose 界面编写、状态管理、按钮事件处理等技能。通过断点调试，熟练掌握了三种调试操作的使用方法。实验过程顺利，所有功能均实现，达到了实验目的，提升了 Android 开发能力。
