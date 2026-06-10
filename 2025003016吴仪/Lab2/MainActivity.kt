package com.example.mainactivitykt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mainactivitykt.ui.theme.MainActivityktTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusinessCard()
        }
    }
}

@Composable
fun BusinessCard(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        CardTop(
            name = "吴仪",
            title = "Android 开发工程师"
        )
        CardBottom(
            phone = "15087378478",
            email = "3506494680@qq.com",
            handle = "芋泥小兔"
        )
    }
}

@Composable
fun CardTop(name: String, title: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 150.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.image),
            contentDescription = "头像",
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = name,
            fontSize = 48.sp,
            color = Color.Black,
            fontWeight = FontWeight.Light
        )
        Text(
            text = title,
            fontSize = 20.sp,
            color = Color(0xFF3DDC84),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CardBottom(phone: String, email: String, handle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(color = Color.LightGray)
        ContactRow(icon = Icons.Default.Phone, info = phone)
        Divider(color = Color.LightGray)
        ContactRow(icon = Icons.Default.Email, info = email)
        Divider(color = Color.LightGray)
        ContactRow(icon = Icons.Default.Share, info = handle)
    }
}

@Composable
fun ContactRow(icon: ImageVector, info: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF3DDC84),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = info,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    BusinessCard()
}
