package com.example.homework4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homework4.ui.theme.Homework4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0XFF073042)
            ){
                BusinessCard()
            }
        }
    }
}

//自定义的函数
@Composable
fun BusinessCard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF073042)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 上半部分：头像 + 姓名 + 职位
        CardTop(
            name = "黄涛",
            title = "渗透测试工程师"
        )
        // 下半部分：联系方式列表
        CardBottom(
            phone = "+86 182-130-95352",
            email = "1429463927@qq.com",
            handle = "@huangtao"
        )
    }
}

@Composable
fun CardTop(name: String, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "头像",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = name, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, color = Color(0xFF3DDC84), fontSize = 18.sp)
    }
}

@Composable
fun ContactRow(
    icon: ImageVector,
    info: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,       // 旁边已有文字说明，传 null 即可
            tint = Color(0xFF3DDC84),        // 与整体配色统一
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = info,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

// CardBottom 中调用
@Composable
fun CardBottom(phone: String, email: String, handle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
    ) {
        Divider(color = Color.Gray.copy(alpha = 0.5f))
        ContactRow(icon = Icons.Default.Phone, info = phone)
        Divider(color = Color.Gray.copy(alpha = 0.5f))
        ContactRow(icon = Icons.Default.Email, info = email)
        Divider(color = Color.Gray.copy(alpha = 0.5f))
        ContactRow(icon = Icons.Default.Share, info = handle)
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview(){
    BusinessCard()
}