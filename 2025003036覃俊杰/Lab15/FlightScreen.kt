package com.example.flightsearch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.FavoriteWithAirports
import com.example.flightsearch.viewmodel.FlightViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightScreen(viewModel: FlightViewModel) {
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedAirport by viewModel.selectedAirport.collectAsState()
    val destinations by viewModel.destinations.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flight Search") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchTextField(
                text = searchText,
                onTextChange = viewModel::updateSearchText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (searchResults.isNotEmpty()) {
                    AutoCompleteList(
                        airports = searchResults,
                        onSelectAirport = viewModel::selectAirport
                    )
                } else if (selectedAirport != null && destinations.isNotEmpty()) {
                    FlightList(
                        departureAirport = selectedAirport!!,
                        destinations = destinations,
                        onToggleFavorite = { destCode ->
                            viewModel.toggleFavorite(selectedAirport!!.iata_code, destCode)
                        },
                        isFavorite = { destCode ->
                            viewModel.isFavorite(selectedAirport!!.iata_code, destCode)
                        }
                    )
                } else if (searchText.isEmpty()) {
                    FavoritesList(
                        favorites = favorites,
                        onSelectAirport = { airport ->
                            viewModel.selectAirport(airport)
                        },
                        onToggleFavorite = { departure, destination ->
                            viewModel.toggleFavorite(departure, destination)
                        }
                    )
                } else {
                    EmptyState()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        placeholder = { Text("输入机场名称或 IATA 代码") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { }
        ),
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun AutoCompleteList(
    airports: List<Airport>,
    onSelectAirport: (Airport) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "自动补全建议",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(airports) { airport ->
                Button(
                    onClick = { onSelectAirport(airport) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${airport.iata_code} - ${airport.name}",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FlightList(
    departureAirport: Airport,
    destinations: List<Airport>,
    onToggleFavorite: (String) -> Unit,
    isFavorite: (String) -> Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "航班列表",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(destinations) { destination ->
                FlightCard(
                    departureCode = departureAirport.iata_code,
                    departureName = departureAirport.name,
                    destinationCode = destination.iata_code,
                    destinationName = destination.name,
                    isFavorite = isFavorite(destination.iata_code),
                    onToggleFavorite = { onToggleFavorite(destination.iata_code) }
                )
            }
        }
    }
}

@Composable
fun FlightCard(
    departureCode: String,
    departureName: String,
    destinationCode: String,
    destinationName: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "出发地: $departureCode - $departureName",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "目的地: $destinationCode - $destinationName",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onToggleFavorite,
                modifier = Modifier.padding(top = 12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (isFavorite) "取消收藏" else "收藏", color = Color.White)
            }
        }
    }
}

@Composable
fun FavoritesList(
    favorites: List<FavoriteWithAirports>,
    onSelectAirport: (Airport) -> Unit,
    onToggleFavorite: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "收藏航线",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        if (favorites.isEmpty()) {
            EmptyState(message = "暂无收藏航线")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { favorite ->
                    FavoriteCard(
                        favorite = favorite,
                        onSelectDeparture = {
                            val airport = Airport(
                                id = 0,
                                iata_code = favorite.departure_code,
                                name = favorite.departure_name,
                                passengers = 0
                            )
                            onSelectAirport(airport)
                        },
                        onToggleFavorite = {
                            onToggleFavorite(favorite.departure_code, favorite.destination_code)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteCard(
    favorite: FavoriteWithAirports,
    onSelectDeparture: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onSelectDeparture,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "出发地: ${favorite.departure_code} - ${favorite.departure_name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "目的地: ${favorite.destination_code} - ${favorite.destination_name}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onToggleFavorite,
                modifier = Modifier.padding(top = 12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "取消收藏", color = Color.White)
            }
        }
    }
}

@Composable
fun EmptyState(message: String = "输入机场名称进行搜索") {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = Color.Gray
        )
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}