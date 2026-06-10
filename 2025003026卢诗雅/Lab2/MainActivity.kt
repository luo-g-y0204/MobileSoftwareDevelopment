package com.example.businesscard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businesscard.ui.theme.BusinessCardTheme

// 主活动入口
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusinessCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFFFF0F5)) { innerPadding ->
                    // 根布局：名片整体
                    BusinessCard(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * 名片根组件：纵向分上下两部分，自定义背景色#073042
 */
@Composable
fun BusinessCard(modifier: Modifier = Modifier) {
    // 自定义颜色常量，统一管理
    val bgColor = Color(0xFFFFF0F5)      // 浅粉色
    val accentColor = Color(0xFFFF69B4) // 玫粉色
    val whiteColor = Color(0xFF777777)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // 所有子元素水平居中
        verticalArrangement = Arrangement.SpaceBetween     // 上下两部分占满屏幕，中间留白
    ) {
        // 上半部分：头像 + 姓名 + 职位
        CardTop(
            name = "卢诗雅",
            title = "Android 开发工程师",
            accentColor = accentColor,
            whiteColor = whiteColor
        )

        // 下半部分：联系方式（带分割线）
        CardBottom(
            phone = "+86 138-8738-6676",
            email = "2110131725@qq.com",
            handle = "@balaba",
            accentColor = accentColor,
            whiteColor = whiteColor
        )
    }
}

/**
 * 名片上半部分：头像 + 姓名 + 职位
 * @param name 姓名
 * @param title 职位
 * @param accentColor 职位文字/图标强调色
 * @param whiteColor 姓名文字白色
 */
@Composable
fun CardTop(
    name: String,
    title: String,
    accentColor: Color,
    whiteColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 60.dp)
    ) {
        // 圆形头像（需在res/drawable下放入avatar.png图片，可替换为自己的头像/Logo）
        Image(
            painter = painterResource(id = R.drawable.avatar), // 头像资源
            contentDescription = "个人头像", // 无障碍描述，非装饰性必须填写
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape) // 裁剪为圆形
        )

        Spacer(modifier = Modifier.height(40.dp)) // 头像与姓名间距

        // 姓名文字
        Text(
            text = name,
            color = whiteColor,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(15.dp)) // 姓名与职位间距

        // 职位文字
        Text(
            text = title,
            color = accentColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp // 字间距，提升美观度
        )
    }
}

/**
 * 名片下半部分：联系方式列表（电话/邮箱/社交）+ 分割线
 * @param phone 手机号
 * @param email 邮箱
 * @param handle 社交账号
 * @param accentColor 图标强调色
 * @param whiteColor 文字白色
 */
@Composable
fun CardBottom(
    phone: String,
    email: String,
    handle: String,
    accentColor: Color,
    whiteColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp)
    ) {
        // 联系方式行 + 分割线（除最后一行外，每行上下都有分割线）
        Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp)
        ContactRow(icon = Icons.Filled.Phone, info = phone, accentColor, whiteColor)
        Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp)
        ContactRow(icon = Icons.Filled.Email, info = email, accentColor, whiteColor)
        Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp)
        ContactRow(icon = Icons.Filled.Share, info = handle, accentColor, whiteColor)
        Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp)
    }
}

/**
 * 单个联系方式行：图标 + 文字（通用封装，复用性强）
 * @param icon 矢量图标
 * @param info 联系方式文字
 * @param accentColor 图标颜色
 * @param textColor 文字颜色
 */
@Composable
fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    info: String,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 25.dp, horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically // 图标与文字垂直居中
    ) {
        // 联系方式图标
        Icon(
            imageVector = icon,
            contentDescription = null, // 旁边有文字说明，装饰性图标传null
            tint = accentColor, // 图标颜色与强调色统一
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(30.dp)) // 图标与文字间距

        // 联系方式文字
        Text(
            text = info,
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * 预览函数：无需运行APP，直接在Android Studio中查看界面效果
 */
@Preview(showBackground = true, name = "名片预览")
@Composable
fun BusinessCardPreview() {
    BusinessCardTheme {
        BusinessCard()
    }
}