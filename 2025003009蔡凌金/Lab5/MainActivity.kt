package com.example.work5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 对应你的三张图片
private val artwork1 = R.drawable.artwork_1
private val artwork2 = R.drawable.artwork_2
private val artwork3 = R.drawable.artwork_3

// 数据类：存储单张作品信息
data class Artwork(
    val imageRes: Int,
    val title: String,
    val artist: String,
    val year: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 使用系统自带主题，无额外依赖
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
    // 当前显示的作品索引
    var currentArtworkIndex by remember { mutableStateOf(0) }

    // 作品列表，对应你的三张图片和英文描述
    val artworkList = listOf(
        Artwork(
            imageRes = artwork1,
            title = "Bamboo Path",
            artist = "Nature Photographer",
            year = "2019"
        ),
        Artwork(
            imageRes = artwork2,
            title = "Coastal Sunset",
            artist = "Landscape Artist",
            year = "2023"
        ),
        Artwork(
            imageRes = artwork3,
            title = "Pixel Panda",
            artist = "Digital Illustrator",
            year = "2020"
        )
    )

    val currentArtwork = artworkList[currentArtworkIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // 图片展示区域
        ArtworkWall(artwork = currentArtwork)
        // 作品信息区域
        ArtworkDescriptor(artwork = currentArtwork)
        // 切换按钮区域
        DisplayController(
            currentIndex = currentArtworkIndex,
            totalCount = artworkList.size,
            onPrevious = {
                currentArtworkIndex = if (currentArtworkIndex == 0) {
                    artworkList.size - 1
                } else {
                    currentArtworkIndex - 1
                }
            },
            onNext = {
                currentArtworkIndex = if (currentArtworkIndex == artworkList.size - 1) {
                    0
                } else {
                    currentArtworkIndex + 1
                }
            }
        )
    }
}

@Composable
fun ArtworkWall(artwork: Artwork, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .width(360.dp)
            .height(480.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        shadowElevation = 10.dp
    ) {
        Image(
            painter = painterResource(id = artwork.imageRes),
            contentDescription = artwork.title,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        )
    }
}

@Composable
fun ArtworkDescriptor(artwork: Artwork, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = artwork.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${artwork.artist} · ${artwork.year}",
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DisplayController(
    currentIndex: Int,
    totalCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPrevious,
            modifier = Modifier
                .width(120.dp)
                .height(50.dp)
        ) {
            Text(text = "Previous", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .width(120.dp)
                .height(50.dp)
        ) {
            Text(text = "Next", fontSize = 16.sp)
        }
    }
}

// 预览函数，无需运行模拟器即可查看效果
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ArtSpaceAppPreview() {
    MaterialTheme {
        ArtSpaceApp()
    }
}