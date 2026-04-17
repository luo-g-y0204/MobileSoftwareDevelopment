package com.example.pener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArtSpaceApp()
                }
            }
        }
    }
}

@Composable
fun ArtSpaceApp() {
    var currentArtwork by remember { mutableIntStateOf(1) }

    // 动态获取图片资源（和你drawable里的名字完全对应）
    val imageResource = when (currentArtwork) {
        1 -> R.drawable.fan_gao      // 梵高《星月夜》
        2 -> R.drawable.da_fen_qi    // 达芬奇作品
        else -> R.drawable.meng_ke   // 蒙克作品
    }

    // 作品信息
    val artworkInfo = when (currentArtwork) {
        1 -> Triple("星月夜", "文森特·梵高", "1889年")
        2 -> Triple("达芬奇作品", "列奥纳多·达·芬奇", "1503-1519年")
        else -> Triple("风景作品", "爱德华·蒙克", "19世纪")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. 作品展示区块
        ArtworkWall(imageResource = imageResource)

        // 2. 作品信息区块
        ArtworkDescriptor(
            title = artworkInfo.first,
            artist = artworkInfo.second,
            year = artworkInfo.third
        )

        // 3. 控制按钮区块
        DisplayController(
            onPrevious = {
                currentArtwork = when (currentArtwork) {
                    1 -> 3
                    else -> currentArtwork - 1
                }
            },
            onNext = {
                currentArtwork = when (currentArtwork) {
                    3 -> 1
                    else -> currentArtwork + 1
                }
            }
        )
    }
}

// 作品展示组件
@Composable
fun ArtworkWall(imageResource: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(400.dp)
            .border(
                width = 8.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            ),
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Image(
            painter = painterResource(id = imageResource),
            contentDescription = "艺术作品",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop // 防止图片拉伸变形
        )
    }
}

// 作品信息组件
@Composable
fun ArtworkDescriptor(title: String, artist: String, year: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "艺术家：$artist", fontSize = 18.sp)
        Text(text = "年份：$year", fontSize = 16.sp, color = Color.Gray)
    }
}

// 控制按钮组件
@Composable
fun DisplayController(onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = onPrevious, modifier = Modifier.width(140.dp)) {
            Text("上一个")
        }
        Button(onClick = onNext, modifier = Modifier.width(140.dp)) {
            Text("下一个")
        }
    }
}