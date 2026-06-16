package com.example.flightsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PageBg       = Color(0xFFFFF9E8)  // 全局页面超浅黄背景
private val CardBg       = Color(0xFFFFFDF0)  // 卡片底色
private val ButtonNormal = Color(0xFFFFCC80)  // 普通按钮浅黄
private val ButtonActive = Color(0xFFFFB74D)  // 选中/取消收藏 稍深橙黄
private val TextPrimary  = Color(0xFFE65100)  // 主文字颜色（深橙）
private val TextHint     = Color(0xFFF57C00)  // 提示文字颜色（橙）
private val LineColor    = Color(0xFFFF9800)  // 输入框边框（橙黄）
private val IconTint     = Color(0xFFEF6C00)  // 返回图标颜色

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightScreen(viewModel: FlightViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val destinations by viewModel.destinations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    if (uiState.selectedAirport != null) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = IconTint
                            )
                        }
                    }
                },
                modifier = Modifier.background(PageBg),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PageBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        modifier = Modifier.background(PageBg)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .background(PageBg)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("机场", color = TextPrimary) },
                placeholder = { Text("输入机场名称或 IATA 代码", color = TextHint) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LineColor,
                    unfocusedBorderColor = LineColor.copy(alpha = 0.7f),
                    focusedLabelColor = TextPrimary,
                    unfocusedLabelColor = TextHint,
                    cursorColor = LineColor,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg
                ),
                shape = RoundedCornerShape(12.dp),  // 搜索框圆角
                singleLine = true
            )

            when {
                uiState.selectedAirport != null -> {
                    Text(
                        text = "航班列表",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    DestinationsList(
                        departureAirport = uiState.selectedAirport!!,
                        destinations = destinations,
                        favorites = uiState.favorites,
                        onToggleFavorite = { destCode ->
                            viewModel.toggleFavorite(uiState.selectedAirport!!.iata_code, destCode)
                        }
                    )
                }
                searchResults.isNotEmpty() -> {
                    Text(
                        text = "自动补全建议",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    SearchResultList(
                        results = searchResults,
                        onSelectAirport = { viewModel.selectAirport(it) }
                    )
                }
                else -> {
                    Text(
                        text = "收藏航线",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    val allAirports by viewModel.allAirports.collectAsState(initial = emptyList())
                    FavoritesList(
                        favorites = uiState.favorites,
                        allAirports = allAirports,
                        onDeleteFavorite = { viewModel.deleteFavorite(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultList(
    results: List<Airport>,
    onSelectAirport: (Airport) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)  // 增加项间距
    ) {
        items(results) { airport ->
            Button(
                onClick = { onSelectAirport(airport) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),  // 统一高度
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonNormal,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                ),
                shape = RoundedCornerShape(10.dp)  // 自动补全项圆角
            ) {
                Text(
                    text = "${airport.iata_code}  ${airport.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DestinationsList(
    departureAirport: Airport,
    destinations: List<Airport>,
    favorites: List<Favorite>,
    onToggleFavorite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)  // 增加卡片间距
    ) {
        items(destinations) { destination ->
            val isFavorite = favorites.any {
                it.departure_code == departureAirport.iata_code &&
                        it.destination_code == destination.iata_code
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),  // 增强阴影
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp)  // 卡片圆角
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "出发：${departureAirport.iata_code} · ${departureAirport.name}",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "到达：${destination.iata_code} · ${destination.name}",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = { onToggleFavorite(destination.iata_code) },
                        modifier = Modifier
                            .align(Alignment.End)  // 按钮右对齐
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFavorite) ButtonActive else ButtonNormal,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        ),
                        shape = RoundedCornerShape(8.dp)  // 收藏按钮圆角
                    ) {
                        Text(
                            text = if (isFavorite) "取消收藏" else "收藏",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesList(
    favorites: List<Favorite>,
    allAirports: List<Airport>,
    onDeleteFavorite: (Favorite) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无收藏航线",
                color = TextHint,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(favorites) { fav ->
                val depAir = allAirports.firstOrNull { it.iata_code == fav.departure_code }
                val destAir = allAirports.firstOrNull { it.iata_code == fav.destination_code }
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "出发地: ${fav.departure_code} · ${depAir?.name ?: ""}",
                            fontSize = 16.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "目的地: ${fav.destination_code} · ${destAir?.name ?: ""}",
                            fontSize = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                            fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = { onDeleteFavorite(fav) },
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonNormal,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            ),
                            shape = RoundedCornerShape(8.dp)  // 取消收藏按钮圆角
                        ) {
                            Text(
                                text = "取消收藏",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}