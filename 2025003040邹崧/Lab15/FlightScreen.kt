package com.example.flightsearch.ui

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.data.entity.Airport
import com.example.flightsearch.data.entity.FavoriteWithNames

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightScreen(viewModel: FlightViewModel = viewModel(
    factory = ViewModelProvider.AndroidViewModelFactory(
        LocalContext.current.applicationContext as Application
    )
)) {
    val searchText by viewModel.searchText.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val selectedAirport by viewModel.selectedAirport.collectAsState()
    val flights by viewModel.flights.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val isSearchActive = searchText.isNotBlank() && selectedAirport == null
    val isFlightListVisible = selectedAirport != null
    val isFavoritesVisible = searchText.isBlank() && selectedAirport == null

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Flight Search") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { viewModel.onSearchTextChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Enter airport name or IATA code") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )

            when {
                isFlightListVisible -> {
                    val departure = selectedAirport ?: return@Column
                    Text(
                        text = "Flights from ${departure.name}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyColumn {
                        items(flights) { destination ->
                            FlightItem(
                                departure = departure,
                                destination = destination,
                                isFavorite = favorites.any {
                                    it.departureCode == departure.iataCode &&
                                            it.destinationCode == destination.iataCode
                                },
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(
                                        departure.iataCode,
                                        destination.iataCode,
                                        favorites.any {
                                            it.departureCode == departure.iataCode &&
                                                    it.destinationCode == destination.iataCode
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
                isSearchActive -> {
                    LazyColumn {
                        items(suggestions) { airport ->
                            SuggestionItem(
                                airport = airport,
                                onClick = { viewModel.onAirportSelected(airport) }
                            )
                        }
                    }
                }
                isFavoritesVisible -> {
                    Text(
                        text = "Saved Favorites",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    if (favorites.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No favorites yet. Search for a flight to add!")
                        }
                    } else {
                        LazyColumn {
                            items(favorites) { fav ->
                                FavoriteItem(
                                    favorite = fav,
                                    onRemove = {
                                        viewModel.toggleFavorite(
                                            fav.departureCode,
                                            fav.destinationCode,
                                            true
                                        )
                                    }
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
fun SuggestionItem(airport: Airport, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "${airport.name} (${airport.iataCode})",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Passengers: ${airport.passengers}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun FlightItem(
    departure: Airport,
    destination: Airport,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "From: ${departure.name} (${departure.iataCode})")
                Text(text = "To: ${destination.name} (${destination.iataCode})")
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun FavoriteItem(favorite: FavoriteWithNames, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "From: ${favorite.departureName} (${favorite.departureCode})")
                Text(text = "To: ${favorite.destinationName} (${favorite.destinationCode})")
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}