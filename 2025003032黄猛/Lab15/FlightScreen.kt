package com.example.flightsearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class AirportUiModel(
    val code: String,
    val city: String,
    val country: String,
    val passengers: Int,
)

data class FlightRouteUiModel(
    val departureCode: String,
    val departureCity: String,
    val arrivalCode: String,
    val arrivalCity: String,
    val duration: String,
    val price: String,
)

private val sampleAirports = listOf(
    AirportUiModel("BJS", "Beijing", "China", 98_000_000),
    AirportUiModel("SHA", "Shanghai", "China", 91_000_000),
    AirportUiModel("CAN", "Guangzhou", "China", 73_000_000),
    AirportUiModel("SZX", "Shenzhen", "China", 72_000_000),
    AirportUiModel("HKG", "Hong Kong", "China", 71_000_000),
    AirportUiModel("SIN", "Singapore", "Singapore", 68_000_000),
    AirportUiModel("NRT", "Tokyo", "Japan", 67_000_000),
    AirportUiModel("ICN", "Seoul", "South Korea", 65_000_000),
    AirportUiModel("HND", "Tokyo Haneda", "Japan", 63_000_000),
    AirportUiModel("DXB", "Dubai", "UAE", 87_000_000),
    AirportUiModel("LAX", "Los Angeles", "USA", 88_000_000),
    AirportUiModel("JFK", "New York", "USA", 62_000_000),
)

private val sampleRoutes = listOf(
    FlightRouteUiModel("BJS", "Beijing", "SHA", "Shanghai", "2h 10m", "¥980"),
    FlightRouteUiModel("BJS", "Beijing", "CAN", "Guangzhou", "3h 00m", "¥1,120"),
    FlightRouteUiModel("BJS", "Beijing", "SZX", "Shenzhen", "3h 10m", "¥1,180"),
    FlightRouteUiModel("BJS", "Beijing", "HKG", "Hong Kong", "3h 20m", "¥1,260"),
    FlightRouteUiModel("BJS", "Beijing", "SIN", "Singapore", "6h 05m", "¥2,680"),
    FlightRouteUiModel("BJS", "Beijing", "NRT", "Tokyo", "2h 55m", "¥1,540"),
    FlightRouteUiModel("BJS", "Beijing", "ICN", "Seoul", "2h 20m", "¥1,330"),
    FlightRouteUiModel("BJS", "Beijing", "HND", "Tokyo Haneda", "3h 00m", "¥1,620"),
    FlightRouteUiModel("BJS", "Beijing", "DXB", "Dubai", "8h 35m", "¥4,880"),
    FlightRouteUiModel("BJS", "Beijing", "LAX", "Los Angeles", "12h 35m", "¥6,920"),
    FlightRouteUiModel("BJS", "Beijing", "JFK", "New York", "14h 00m", "¥7,480"),
    FlightRouteUiModel("BJS", "Beijing", "SHA", "Shanghai", "2h 05m", "¥1,050"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightScreen(modifier: Modifier = Modifier) {
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedAirportCode by rememberSaveable { mutableStateOf(sampleAirports.first().code) }
    val favoriteKeys = remember { mutableStateListOf<String>() }

    val matchingAirports = remember(searchText) {
        if (searchText.isBlank()) {
            emptyList()
        } else {
            sampleAirports.filter {
                it.code.contains(searchText, ignoreCase = true) ||
                    it.city.contains(searchText, ignoreCase = true) ||
                    it.country.contains(searchText, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(searchText) {
        if (searchText.isNotBlank()) {
            matchingAirports.firstOrNull()?.let { selectedAirportCode = it.code }
        }
    }

    val selectedAirport = sampleAirports.firstOrNull { it.code == selectedAirportCode }
        ?: sampleAirports.first()

    val selectedRoutes = sampleRoutes.filter { it.departureCode == selectedAirport.code }
    val favoriteRoutes = sampleRoutes.filter { favoriteKeys.contains(routeKey(it)) }

    Scaffold(
        containerColor = Color(0xFFF4F7FB),
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                FlightHeroCard(
                    airport = selectedAirport,
                    routeCount = selectedRoutes.size,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                FlightSearchField(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (searchText.isBlank()) {
                item {
                    SectionCard(
                        title = "收藏航线",
                        subtitle = "搜索为空时展示收藏列表",
                    ) {
                        if (favoriteRoutes.isEmpty()) {
                            Text(
                                text = "还没有收藏，先从下方热门航线里挑一条吧。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF5B6576),
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                favoriteRoutes.forEach { route ->
                                    FlightRouteCard(
                                        route = route,
                                        isFavorite = true,
                                        onFavoriteToggle = {
                                            favoriteKeys.remove(routeKey(route))
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    SectionCard(
                        title = "自动补全建议",
                        subtitle = "点选机场后会刷新航班列表",
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            matchingAirports.take(8).forEach { airport ->
                                AirportSuggestionRow(
                                    airport = airport,
                                    selected = airport.code == selectedAirport.code,
                                    onClick = {
                                        selectedAirportCode = airport.code
                                        searchText = airport.code
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = if (searchText.isBlank()) "热门航线" else "航班列表",
                    subtitle = if (searchText.isBlank()) {
                        "展示更多可收藏的示例航线"
                    } else {
                        "当前选择：${selectedAirport.city} (${selectedAirport.code})"
                    },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        selectedRoutes.forEach { route ->
                            FlightRouteCard(
                                route = route,
                                isFavorite = favoriteKeys.contains(routeKey(route)),
                                onFavoriteToggle = {
                                    val key = routeKey(route)
                                    if (favoriteKeys.contains(key)) {
                                        favoriteKeys.remove(key)
                                    } else {
                                        favoriteKeys.add(key)
                                    }
                                },
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "机场概览",
                    subtitle = "超过 10 条示例数据",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sampleAirports.take(10).forEach { airport ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "${airport.code} · ${airport.city}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = "${airport.passengers / 1_000_000}M",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF5B6576),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightHeroCard(
    airport: AirportUiModel,
    routeCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF0EA5E9), Color(0xFFF59E0B)),
                    )
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    imageVector = Icons.Filled.FlightTakeoff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "Flight Search",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${airport.city} (${airport.code}) · $routeCount 条直达示例",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
        }
    }
}

@Composable
private fun FlightSearchField(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
            )
        },
        trailingIcon = if (searchText.isNotBlank()) {
            {
                IconButton(onClick = { onSearchTextChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "清空搜索",
                    )
                }
            }
        } else {
            null
        },
        placeholder = { Text("输入机场代码或城市名") },
        label = { Text("搜索机场") },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                )
            }
            content()
        }
    }
}

@Composable
private fun AirportSuggestionRow(
    airport: AirportUiModel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = if (selected) Color(0xFFDCEEFF) else Color(0xFFF8FAFC),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "${airport.city} · ${airport.code}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = airport.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5B6576),
                )
            }
            Text(
                text = "${airport.passengers / 1_000_000}M",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF0369A1),
            )
        }
    }
}

@Composable
private fun FlightRouteCard(
    route: FlightRouteUiModel,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${route.departureCity} (${route.departureCode}) → ${route.arrivalCity} (${route.arrivalCode})",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${route.duration} · ${route.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5B6576),
                )
            }
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "取消收藏" else "添加收藏",
                    tint = if (isFavorite) Color(0xFFDC2626) else Color(0xFF64748B),
                )
            }
        }
    }
}

private fun routeKey(route: FlightRouteUiModel): String {
    return "${route.departureCode}-${route.arrivalCode}"
}

@Preview(showBackground = true)
@Composable
private fun FlightScreenPreview() {
    MaterialTheme {
        FlightScreen()
    }
}
