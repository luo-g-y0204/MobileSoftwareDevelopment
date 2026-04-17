package com.example.artspaceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.artspaceapp.ui.theme.ArtSpaceAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtSpaceAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArtSpaceScreen()
                }
            }
        }
    }
}

@Composable
fun ArtSpaceScreen() {
    // 当前作品编号
    var currentArt by remember { mutableStateOf(1) }

    // 图片资源
    val imageRes = when (currentArt) {
        1 -> R.drawable.art1
        2 -> R.drawable.art2
        else -> R.drawable.art3
    }

    // 作品信息
    val title = when (currentArt) {
        1 -> "Life"
        2 -> "Flowers"
        else -> "Sunflower"
    }

    val artist = when (currentArt) {
        1 -> "ChengYuLong"
        2 -> "van Gogh"
        else -> "van Gogh"
    }

    val year = when (currentArt) {
        1 -> "2020"
        2 -> "1941"
        else -> "1945"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Art Space",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 30.dp)
        )

        // 作品图片（带画框）
        Surface(
            modifier = Modifier
                .size(340.dp)
                .border(4.dp, Color.LightGray)
                .clip(MaterialTheme.shapes.medium),
            color = Color.White
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = "Artwork",
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(30.dp))

        // 作品信息
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = "$artist • $year", fontSize = 16.sp, color = Color.Gray)
        }

        Spacer(Modifier.height(40.dp))

        // 上一张 / 下一张
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    currentArt = when (currentArt) {
                        1 -> 3
                        else -> currentArt - 1
                    }
                },
                modifier = Modifier.width(130.dp)
            ) {
                Text("Previous")
            }

            Spacer(Modifier.width(30.dp))

            Button(
                onClick = {
                    currentArt = when (currentArt) {
                        3 -> 1
                        else -> currentArt + 1
                    }
                },
                modifier = Modifier.width(130.dp)
            ) {
                Text("Next")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArtSpacePreview() {
    ArtSpaceAppTheme {
        ArtSpaceScreen()
    }
}