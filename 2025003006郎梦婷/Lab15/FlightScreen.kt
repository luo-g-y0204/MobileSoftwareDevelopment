package com.example.flightsearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
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
                    if (uiState.selectedAirport != null) {
                        Text(text = "${uiState.selectedAirport?.iata_code} Destinations")
                    } else {
                        Text(text = "Flight Search")
                    }
                },
                navigationIcon = {
                    if (uiState.selectedAirport != null) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = {},
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            when {
                uiState.selectedAirport != null -> {
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
                    SearchResultsList(
                        results = searchResults,
                        onSelectAirport = { viewModel.selectAirport(it) }
                    )
                }
                else -> {
                    FavoritesList(
                        favorites = uiState.favorites,
                        onSelectFavorite = { depCode, destCode ->
                            viewModel.updateSearchQuery(depCode)
                        },
                        onDeleteFavorite = { viewModel.deleteFavorite(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsList(
    results: List<Airport>,
    onSelectAirport: (Airport) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(results) { airport ->
            AirportCard(
                airport = airport,
                onClick = { onSelectAirport(airport) }
            )
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
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(destinations) { destination ->
            val isFavorite = favorites.any {
                it.departure_code == departureAirport.iata_code &&
                it.destination_code == destination.iata_code
            }
            DestinationCard(
                departure = departureAirport,
                destination = destination,
                isFavorite = isFavorite,
                onToggleFavorite = { onToggleFavorite(destination.iata_code) }
            )
        }
    }
}

@Composable
fun FavoritesList(
    favorites: List<Favorite>,
    onSelectFavorite: (String, String) -> Unit,
    onDeleteFavorite: (Favorite) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorite flights yet",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(favorites) { favorite ->
                    FavoriteCard(
                        favorite = favorite,
                        onClick = { onSelectFavorite(favorite.departure_code, favorite.destination_code) },
                        onDelete = { onDeleteFavorite(favorite) }
                    )
                }
            }
        }
    }
}

@Composable
fun AirportCard(
    airport: Airport,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = airport.iata_code,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = airport.name,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "Passengers: ${airport.passengers}",
                fontSize = 14.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun DestinationCard(
    departure: Airport,
    destination: Airport,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${departure.iata_code} -> ${destination.iata_code}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = destination.name,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}

@Composable
fun FavoriteCard(
    favorite: Favorite,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${favorite.departure_code} -> ${favorite.destination_code}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.Red,
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = onDelete) {
                    Text(
                        text = "Delete",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}