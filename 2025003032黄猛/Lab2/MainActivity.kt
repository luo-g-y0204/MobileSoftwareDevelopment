package com.example.myapplication3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication3.ui.theme.MyApplication3Theme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication3.CardTop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF073042)
            ) {
                BusinessCard()
            }
        }
    }
}

@Composable
fun BusinessCard() {
    val cardBgColor = Color(0xFF7085F6)
    val mainColor = Color(0xFFB0EF66)
    val textWhite = Color(0xFFffffff)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cardBgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 名片上半部分：头像+姓名+职位
        CardTop(
            name = "HuangMneg",
            title = "Android 开发工程师",
            mainColor = mainColor,
            textWhite = textWhite
        )
        Spacer(modifier = Modifier.height(40.dp))
        // 名片下半部分：联系方式（已移除所有分隔线）
        CardBottom(
            phone = "+86 111-2222-3333",
            email = "huangMeng@example.com",
            handle = "@huangmeng",
            mainColor = mainColor,
            textWhite = textWhite
        )
    }
}

@Composable
fun CardTop(
    name: String,
    title: String,
    mainColor: Color,
    textWhite: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 60.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        // 1. 定义 Box 容器
        Box(
            modifier = Modifier.size(120.dp)
        ) {
            // 底层：背景图
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "背景",
                modifier = Modifier.fillMaxSize()
            )

            // 中层：文字（放在 Box 底部，居中）
            Text(
                text = "Android", // 你的单词
                modifier = Modifier
                    .align(Alignment.BottomCenter) // 关键：贴在底部
                    .padding(bottom = 8.dp), // 离底部留点空，不贴边
                color = Color.White,
                fontSize = 24.sp
            )

            // 顶层：图标（放在正中间）
            Image(
                painter = painterResource(id = R.drawable.android),
                contentDescription = "图标",
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.Center)// 关键：居中
                    .offset(y = -15.dp) //y 往下移，x 左右
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = name,
            color = textWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            color = mainColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CardBottom(
    phone: String,
    email: String,
    handle: String,
    mainColor: Color,
    textWhite: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.width(320.dp), // 统一最大宽度，可按需调整
            horizontalAlignment = Alignment.Start // 内部左对齐，保证图标和文字对齐
        ) {
            // 直接调用联系方式行，无任何分隔线
            ContactRow(
                icon = Icons.Default.Phone,
                info = phone,
                mainColor = mainColor,
                textColor = textWhite
            )
            ContactRow(
                icon = Icons.Default.Email,
                info = email,
                mainColor = mainColor,
                textColor = textWhite
            )
            ContactRow(
                icon = Icons.Default.Share,
                info = handle,
                mainColor = mainColor,
                textColor = textWhite
            )
        }
    }
}

// 通用联系方式行：图标+文字
@Composable
fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    info: String,
    mainColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = mainColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = info,
            color = textColor,
            fontSize = 16.sp,

        )
    }
}

// 预览函数：直接在Android Studio查看效果
@Preview(showBackground = true, name = "名片预览")
@Composable
fun BusinessCardPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF073042)
    ) {
        BusinessCard()
    }
}