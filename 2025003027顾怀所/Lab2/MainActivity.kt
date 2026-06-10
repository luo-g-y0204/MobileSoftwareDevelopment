package com.example.businesscard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BusinessCard()
                }
            }
        }
    }
}

@Composable
fun BusinessCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // 月白星紫渐变背景
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F8FF), // 月白色
                        Color(0xFF9370DB)  // 星紫色
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 100.dp, bottom = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // 上半部分：头像 + 姓名 + 职位
            CardTop(
                name = "顾怀所",
                title = "Android 开发工程师"
            )

            // 下半部分：联系方式
            CardBottom(
                phone = "+86 18468061212",
                email = "2596415305@qq.com",
                social = "gmhjs"
            )
        }
    }
}

// 上半部分：头像 + 姓名 + 职位
@Composable
fun CardTop(name: String, title: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 头像
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "头像",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .padding(4.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = name,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = title,
            color = Color(0xFFE6E6FA),
            fontSize = 20.sp
        )
    }
}

// 下半部分：联系方式列表
@Composable
fun CardBottom(phone: String, email: String, social: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
        .padding(top = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ContactRow(icon = Icons.Default.Phone, info = phone)
        ContactRow(icon = Icons.Default.Email, info = email)
        ContactRow(icon = Icons.Default.Share, info = social)
    }
}

// 单行联系方式：图标 + 文字
@Composable
fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, info: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF9370DB),
            modifier = Modifier.size(26.dp)
        )

        Text(
            text = info,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}