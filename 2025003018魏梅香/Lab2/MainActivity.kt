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
            BusinessCard()
        }
    }
}

//主布局：整个名片页面
@Composable
fun BusinessCard(){
    //背景颜色:浅紫色
    val backgroundDark= Color(0xFFF0E6F6)
    //整体垂直布局
    Column(modifier =
        Modifier.fillMaxSize().background(backgroundDark),//占满整个屏幕，设置背景色
        horizontalAlignment = Alignment.CenterHorizontally, //所有元素水平居中
        verticalArrangement = Arrangement.Top)//上下两部分自动分开
    {
        // 上半部分
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 上半部分内部垂直居中
        ) {
            CardTop(
                name="魏梅香",
                title="Android 开发工程师"
            )
        }

        // 下半部分
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally, //水平居中
            verticalArrangement = Arrangement.Bottom // 下半部分内部靠底
        ) {
            CardBottom(
                phone = "+86 191-4327-4620",
                social = "@weimeixiang",
                email = "3539261624@qq.com"
            )
        }
    }
}


//上半部分：头像+姓名+职位
@Composable
fun CardTop(name:String,title:String){
    //垂直布局，顶部留间距
    Column(horizontalAlignment = Alignment.CenterHorizontally)
    {
        //头像
        Image(
            painter = painterResource(id=R.drawable.logo),
            contentDescription = "头像",
            modifier = Modifier.size(120.dp).clip(CircleShape)
        )
        //姓名
        Text(
            text=name,
            color= Color.Blue,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top=16.dp)
        )
        //职位
        Text(
            text = title,
            color=Color.DarkGray,
            fontSize = 18.sp,
            modifier = Modifier.padding(top=8.dp)
        )
    }
}


//下半部分：联系方式列表
@Composable
fun CardBottom(phone:String, social: String, email:String){
    Column(
        horizontalAlignment = Alignment.Start // 保持行内左对齐
    ){
        ContactRow(icon=Icons.Default.Phone, info=phone)
        ContactRow(icon=Icons.Default.Share, info=social)
        ContactRow(icon=Icons.Default.Email, info=email)
    }
}

@Composable
fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, info: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 12.dp), // 整体居中
        verticalAlignment = Alignment.CenterVertically // 图标和文字垂直居中
    ) {
        // 图标
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF3DDC84), // Android绿色
            modifier = Modifier.size(24.dp)
        )

        // 图标和文字之间的间距
        Spacer(modifier = Modifier.width(24.dp))

        // 文字
        Text(
            text = info,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}


