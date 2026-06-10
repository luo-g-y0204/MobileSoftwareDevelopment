package com.example.mainactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import com.example.mainactivity.ui.theme.MainActivityTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainActivityTheme {
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

@Composable
fun BusinessCard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2E9)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 顶部图标
        Image(
            painter = painterResource(R.drawable.xiaodog),
            contentDescription = null,
            modifier = Modifier.size(180.dp)
        )

        Spacer(Modifier.height(24.dp))

        // 名字
        Text(
            text = "安芮佳",
            fontSize = 48.sp,
            fontWeight = FontWeight.Light
        )

        Spacer(Modifier.height(16.dp))

        // 职位
        Text(
            text = "The discerning gourmet",
            color = Color(0xFF2E7D32),
            fontSize = 20.sp
        )

        Spacer(Modifier.height(120.dp))

        // 联系方式
        ContactItem(
            icon = { PhoneIcon() },
            text = "+11 (123) 444 555 666"
        )
        ContactItem(
            icon = { ShareIcon() },
            text = "@Anruijia_Tua"
        )
        ContactItem(
            icon = { EmailIcon() },
            text = "An.Tua@android.com"
        )
    }
}

@Composable
fun ContactItem(icon: @Composable () -> Unit, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.width(24.dp))
        Text(text = text, fontSize = 18.sp)
    }
}

@Composable
fun PhoneIcon() {
    Icon(
        imageVector = Icons.Default.Phone,
        contentDescription = null,
        tint = Color(0xFF2E7D32)
    )
}

@Composable
fun ShareIcon() {
    Icon(
        imageVector = Icons.Default.Share,
        contentDescription = null,
        tint = Color(0xFF2E7D32)
    )
}

@Composable
fun EmailIcon() {
    Icon(
        imageVector = Icons.Default.Email,
        contentDescription = null,
        tint = Color(0xFF2E7D32)
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainActivityTheme {
        BusinessCard()
    }
}