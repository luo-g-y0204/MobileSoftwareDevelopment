package com.example.flightsearch.ui

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.flightsearch.data.local.Airport
import com.example.flightsearch.data.local.FavoriteRoute
import com.example.flightsearch.ui.theme.FlightSearchTheme

private enum class ScreenMode {
    Loading,
    Favorites,
    Suggestions,
    Flights,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightScreen(
    uiState: FlightUiState,
    onSearchTextChanged: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
    onClearSearch: () -> Unit,
    onToggleFavorite: (FlightItem) -> Unit,
    onRemoveFavorite: (FavoriteRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                        )
                        Text("Flight Search")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SearchField(
                value = uiState.searchText,
                onValueChange = onSearchTextChanged,
                onClear = onClearSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            val screenMode = when {
                uiState.isLoading -> ScreenMode.Loading
                uiState.searchText.isBlank() -> ScreenMode.Favorites
                uiState.selectedAirport == null -> ScreenMode.Suggestions
                else -> ScreenMode.Flights
            }

            Crossfade(
                targetState = screenMode,
                label = "flight_screen_switch",
                modifier = Modifier.fillMaxSize(),
            ) { mode ->
                when (mode) {
                    ScreenMode.Loading -> LoadingContent()
                    ScreenMode.Favorites -> FavoritesContent(
                        favorites = uiState.favorites,
                        onRemoveFavorite = onRemoveFavorite,
                    )
                    ScreenMode.Suggestions -> SuggestionsContent(
                        query = uiState.searchText,
                        suggestions = uiState.suggestions,
                        onAirportSelected = onAirportSelected,
                    )
                    ScreenMode.Flights -> {
                        val airport = uiState.selectedAirport
                        if (airport == null) {
                            LoadingContent()
                        } else {
                            FlightsContent(
                                airport = airport,
                                flights = uiState.flights,
                                onToggleFavorite = onToggleFavorite,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        label = { Text("机场名称或 IATA 代码") },
        placeholder = { Text("例如：LAX、London") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清空搜索",
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
        ),
        shape = RoundedCornerShape(18.dp),
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SuggestionsContent(
    query: String,
    suggestions: List<Airport>,
    onAirportSelected: (Airport) -> Unit,
) {
    if (suggestions.isEmpty()) {
        EmptyMessage(
            title = "没有找到匹配机场",
            description = "尝试输入机场名称或三个字母的 IATA 代码。",
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "“$query”的自动补全",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        items(
            items = suggestions,
            key = { it.id },
        ) { airport ->
            AirportSuggestionCard(
                airport = airport,
                onClick = { onAirportSelected(airport) },
            )
        }
    }
}

@Composable
private fun AirportSuggestionCard(
    airport: Airport,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = airport.name,
                    fontWeight = FontWeight.Medium,
                )
            },
            supportingContent = {
                Text("年客流量：${airport.passengers}")
            },
            leadingContent = {
                IataBadge(airport.iataCode)
            },
        )
    }
}

@Composable
private fun FlightsContent(
    airport: Airport,
    flights: List<FlightItem>,
    onToggleFavorite: (FlightItem) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "从 ${airport.iataCode} 出发",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = airport.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(
            items = flights,
            key = { "${it.departureCode}-${it.destinationCode}" },
        ) { flight ->
            FlightCard(
                flight = flight,
                onToggleFavorite = { onToggleFavorite(flight) },
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun FlightCard(
    flight: FlightItem,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RouteAirportLine(
                    code = flight.departureCode,
                    name = flight.departureName,
                    label = "出发",
                )
                RouteAirportLine(
                    code = flight.destinationCode,
                    name = flight.destinationName,
                    label = "到达",
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (flight.isFavorite) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = if (flight.isFavorite) "取消收藏" else "添加收藏",
                    tint = if (flight.isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    favorites: List<FavoriteRoute>,
    onRemoveFavorite: (FavoriteRoute) -> Unit,
) {
    if (favorites.isEmpty()) {
        EmptyMessage(
            title = "还没有收藏航线",
            description = "搜索机场并点击航线右侧的爱心即可收藏。",
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "收藏航线",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        items(
            items = favorites,
            key = { it.id },
        ) { favorite ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RouteAirportLine(
                            code = favorite.departureCode,
                            name = favorite.departureName,
                            label = "出发",
                        )
                        RouteAirportLine(
                            code = favorite.destinationCode,
                            name = favorite.destinationName,
                            label = "到达",
                        )
                    }
                    IconButton(onClick = { onRemoveFavorite(favorite) }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "取消收藏",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun RouteAirportLine(
    code: String,
    name: String,
    label: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IataBadge(code)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun IataBadge(code: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(width = 58.dp, height = 42.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = code,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun EmptyMessage(
    title: String,
    description: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.FlightTakeoff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FlightScreenPreview() {
    FlightSearchTheme {
        FlightScreen(
            uiState = FlightUiState(
                searchText = "LAX",
                selectedAirport = Airport(
                    id = 1,
                    name = "Los Angeles International Airport",
                    iataCode = "LAX",
                    passengers = 88068013,
                ),
                flights = listOf(
                    FlightItem(
                        departureCode = "LAX",
                        departureName = "Los Angeles International Airport",
                        destinationCode = "SFO",
                        destinationName = "San Francisco International Airport",
                        isFavorite = true,
                    ),
                ),
                isLoading = false,
            ),
            onSearchTextChanged = {},
            onAirportSelected = {},
            onClearSearch = {},
            onToggleFavorite = {},
            onRemoveFavorite = {},
        )
    }
}
