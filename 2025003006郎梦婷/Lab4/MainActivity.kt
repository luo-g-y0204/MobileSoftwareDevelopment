package com.example.diceroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent 本身就是 @Composable 环境，直接在这里写 UI，彻底解决调用错误
        setContent {
            MaterialTheme {
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

// 纯 @Composable 函数，内部调用所有组件完全合法
@Composable
fun DiceRollerApp() {
    // 用 mutableIntStateOf 消除警告，性能更优
    val diceNumber = remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imageResource = when (diceNumber.intValue) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }

        Image(
            painter = painterResource(id = imageResource),
            contentDescription = diceNumber.intValue.toString(),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(onClick = {
            diceNumber.intValue = (1..6).random()
        }) {
            Text(text = "Roll")
        }
    }
}

// 预览函数，用 MaterialTheme 包裹
@Preview(showBackground = true)
@Composable
fun DiceRollerPreview() {
    MaterialTheme {
        DiceRollerApp()
    }
}