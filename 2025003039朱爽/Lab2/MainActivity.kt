package com.example.work2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFF0F7)
                ) {
                    BusinessCard()
                }
            }
        }
    }
}

@Composable
fun BusinessCard() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 上半部分：头像 + 姓名 + 职位
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 120.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "个人头像",
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape) // 圆形头像
            )

            Text(
                text = "朱爽",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD16A8F),
                modifier = Modifier.padding(top = 18.dp)
            )

            Text(
                text = "软件设计师",
                fontSize = 18.sp,
                color = Color(0xFFD16A8F),
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // 下半部分：联系方式
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(bottom = 60.dp)
                .padding(horizontal = 40.dp)
        ) {
            ContactItem(icon = Icons.Filled.Phone, text = "+86 199 0000 0000")
            ContactItem(icon = Icons.Filled.Share, text = "@ZHUZHUXIA")
            ContactItem(icon = Icons.Filled.Email, text = "zhushuang@android.com")
        }
    }
}

@Composable
fun ContactItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFD16A8F), // 淡粉色图标
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = Color(0xFFD16A8F)
        )
    }
}