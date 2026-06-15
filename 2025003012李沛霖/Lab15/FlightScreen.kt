package com.example.flightsearch.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.flightsearch.data.FavoriteRoute
import com.example.flightsearch.data.FlightRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchScreen() {
    val context = LocalContext.current
    val viewModel: FlightViewModel = viewModel(factory = FlightViewModelFactory(context))

    // 收集UI所需数据流
    val searchText = viewModel.searchText.collectAsStateWithLifecycle()
    val airportSuggestions = viewModel.airportSuggestions.collectAsStateWithLifecycle(initialValue = emptyList())
    val flightList = viewModel.flightList.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteRoutes = viewModel.favoriteRoutes.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedDepartAirport = viewModel.selectedDepartAirport.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Flight Search 航班查询") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            // 搜索输入框
            OutlinedTextField(
                value = searchText.value,
                onValueChange = viewModel::updateSearchText,
                label = { Text("输入机场IATA代码 / 机场名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 自动补全下拉列表
            if (searchText.value.isNotBlank() && airportSuggestions.value.isNotEmpty() && selectedDepartAirport.value == null) {
                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                    items(airportSuggestions.value) { airport ->
                        ListItem(
                            headlineContent = { Text("${airport.iata_code} | ${airport.name}") },
                            modifier = Modifier.clickable { viewModel.selectAirport(airport) }
                        )
                    }
                }
                Divider()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 界面分支逻辑
            when {
                // 选中出发机场：展示航班列表
                selectedDepartAirport.value != null -> {
                    Text(
                        text = "出发机场：${selectedDepartAirport.value!!.iata_code} ${selectedDepartAirport.value!!.name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(flightList.value) { flightRoute ->
                            FlightListItem(route = flightRoute, onToggleFavorite = viewModel::toggleFavorite)
                            Divider()
                        }
                    }
                }
                // 搜索框为空：展示收藏列表
                searchText.value.isBlank() -> {
                    Text(
                        text = "我的收藏航线",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (favoriteRoutes.value.isEmpty()) {
                        Text(text = "暂无收藏航班，搜索航线后点击爱心添加收藏")
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(favoriteRoutes.value) { favRoute ->
                            FavoriteListItem(route = favRoute, onToggleFavorite = viewModel::toggleFavorite)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

// 航班列表单项组件
@Composable
fun FlightListItem(route: FlightRoute, onToggleFavorite: (String, String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "出发：${route.departAirport.iata_code} ${route.departAirport.name}")
            Text(text = "到达：${route.destAirport.iata_code} ${route.destAirport.name}")
        }
        IconButton(onClick = { onToggleFavorite(route.departAirport.iata_code, route.destAirport.iata_code) }) {
            Icon(
                imageVector = if (route.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "收藏按钮"
            )
        }
    }
}

// 收藏列表单项组件
@Composable
fun FavoriteListItem(route: FavoriteRoute, onToggleFavorite: (String, String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "${route.departureCode} ${route.departureName} → ${route.destinationCode} ${route.destinationName}")
        IconButton(onClick = { onToggleFavorite(route.departureCode, route.destinationCode) }) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "取消收藏"
            )
        }
    }
}