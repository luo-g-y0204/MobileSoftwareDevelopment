package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // 应用整体背景
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BusinessCard()
                }
            }
        }
    }
}

// 主名片布局
@Composable
fun BusinessCard() {
    // 自定义颜色
    val primaryColor = Color(0xFF3DDC84)
    val bgColor = Color(0xFF073042)
    val textColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        CardTop(
            name = "徐彬皓",
            title = "死滅回遊 特级咒术师",
            primaryColor = primaryColor,
            textColor = textColor
        )


        CardBottom(
            phone = "+86 157-5815-8426",
            email = "xubinaho@qq.com",
            handle = "@Tejizhoushushi_Xu",
            primaryColor = primaryColor,
            textColor = textColor
        )
    }
}

// 名片上半部分
@Composable
fun CardTop(
    name: String,
    title: String,
    primaryColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 头像（需在 res/drawable 下放置 logo.png 图片）
        Image(
            painter = painterResource(R.drawable.yuta),
            contentDescription = "个人头像",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )


        Spacer(modifier = Modifier.height(24.dp))

        // 姓名
        Text(
            text = name,
            color = textColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        // 间距
        Spacer(modifier = Modifier.height(8.dp))

        // 职位
        Text(
            text = title,
            color = primaryColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// 单行联系方式组件
@Composable
fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    info: String,
    primaryColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // 文字已说明，装饰性图标传 null
            tint = primaryColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = info,
            color = textColor,
            fontSize = 16.sp
        )
    }
}


@Composable
fun CardBottom(
    phone: String,
    email: String,
    handle: String,
    primaryColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(bottom = 60.dp)
    ) {
        // 分隔线
        Divider(color = Color.Gray.copy(alpha = 0.5f))

        // 电话行
        ContactRow(
            icon = Icons.Default.Phone,
            info = phone,
            primaryColor = primaryColor,
            textColor = textColor
        )

        Divider(color = Color.Gray.copy(alpha = 0.5f))

        // 邮件行
        ContactRow(
            icon = Icons.Default.Email,
            info = email,
            primaryColor = primaryColor,
            textColor = textColor
        )

        Divider(color = Color.Gray.copy(alpha = 0.5f))

        // 社交账号行
        ContactRow(
            icon = Icons.Default.Share,
            info = handle,
            primaryColor = primaryColor,
            textColor = textColor
        )

        Divider(color = Color.Gray.copy(alpha = 0.5f))
    }
}

// 预览函数
@Preview(showBackground = true)
@Composable
fun BusinessCardPreview() {
    MaterialTheme {
        BusinessCard()
    }
}