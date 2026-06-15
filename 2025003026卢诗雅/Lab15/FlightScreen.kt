package com.example.flightsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// 浅粉色系（增加深浅层次：背景<卡片<普通按钮<激活按钮）
private val PageBg       = Color(0xFFFFF2F7)  // 全局背景：最浅粉
private val CardBg       = Color(0xFFFFE9F2)  // 卡片背景：中等浅粉
private val ButtonNormal = Color(0xFFF8B8D0)  // 普通按钮：偏深浅粉
private val ButtonActive = Color(0xFFF298BC)  // 已收藏按钮：最深浅粉
private val TextPrimary  = Color(0xFF72384C)  // 主文字
private val TextHint     = Color(0xFFB0788C)  // 提示文字
private val LineColor    = Color(0xFFE898B8)  // 输入框边框
private val IconTint     = Color(0xFFD87898)  // 返回图标

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
                        "Flight Search",
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
                modifier = Modifier.background(PageBg)
            )
        },
        modifier = Modifier.background(PageBg)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .background(PageBg)
        ) {
            // 搜索框
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("机场", color = TextPrimary) },
                placeholder = { Text("输入机场名称或 IATA 代码", color = TextHint) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LineColor,
                    unfocusedBorderColor = LineColor,
                    focusedLabelColor = TextPrimary,
                    unfocusedLabelColor = TextHint,
                    cursorColor = LineColor
                ),
                singleLine = true
            )

            // 页面状态分发
            when {
                // 1. 已选择出发机场：展示航班列表
                uiState.selectedAirport != null -> {
                    Text(
                        text = "航班列表",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
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
                // 2. 有搜索内容且有结果：展示搜索建议
                searchResults.isNotEmpty() -> {
                    Text(
                        text = "自动补全建议",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    SearchResultList(
                        results = searchResults,
                        onSelectAirport = { viewModel.selectAirport(it) }
                    )
                }
                // 3. 无搜索、无选中机场：展示收藏航线
                else -> {
                    Text(
                        text = "收藏航线",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
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

// 搜索结果列表
@Composable
fun SearchResultList(
    results: List<Airport>,
    onSelectAirport: (Airport) -> Unit
) {
    LazyColumn {
        items(results) { airport ->
            Button(
                onClick = { onSelectAirport(airport) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Transparent
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ButtonNormal)
                        .padding(8.dp)
                ) {
                    Text(text = "${airport.iata_code}  ${airport.name}", color = Color.White)
                }
            }
        }
    }
}

// 目的地航班列表
@Composable
fun DestinationsList(
    departureAirport: Airport,
    destinations: List<Airport>,
    favorites: List<Favorite>,
    onToggleFavorite: (String) -> Unit
) {
    LazyColumn {
        items(destinations) { destination ->
            val isFavorite = favorites.any {
                it.departure_code == departureAirport.iata_code &&
                        it.destination_code == destination.iata_code
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "出发：${departureAirport.iata_code} · ${departureAirport.name}",
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "到达：${destination.iata_code} · ${destination.name}",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )

                    Button(
                        onClick = { onToggleFavorite(destination.iata_code) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Transparent
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (isFavorite) ButtonActive else ButtonNormal)
                                .padding(6.dp)
                        ) {
                            Text(text = if (isFavorite) "取消收藏" else "收藏", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// 收藏列表
@Composable
fun FavoritesList(
    favorites: List<Favorite>,
    allAirports: List<Airport>,
    onDeleteFavorite: (Favorite) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "暂无收藏航线", color = TextHint, fontSize = 17.sp)
        }
    } else {
        LazyColumn {
            items(favorites) { fav ->
                val depAir = allAirports.firstOrNull { it.iata_code == fav.departure_code }
                val destAir = allAirports.firstOrNull { it.iata_code == fav.destination_code }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "出发地: ${fav.departure_code} · ${depAir?.name ?: ""}",
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "目的地: ${fav.destination_code} · ${destAir?.name ?: ""}",
                            fontSize = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        Button(
                            onClick = { onDeleteFavorite(fav) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Transparent
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(ButtonNormal)
                                    .padding(6.dp)
                            ) {
                                Text(text = "取消收藏", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}