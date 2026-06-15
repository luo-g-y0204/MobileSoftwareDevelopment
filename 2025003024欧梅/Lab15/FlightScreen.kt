package com.example.flightsearchlab15.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flightsearchlab15.data.Airport
import com.example.flightsearchlab15.data.FavoriteRoute
import com.example.flightsearchlab15.viewmodel.FlightViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchScreen(
    viewModel: FlightViewModel,
    modifier: Modifier = Modifier
) {
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val autoCompleteItems by viewModel.autoCompleteList.collectAsStateWithLifecycle(emptyList())
    val allFavorites by viewModel.favoriteRoutes.collectAsStateWithLifecycle(emptyList())

    var selectedDepartAirport by remember { mutableStateOf<Airport?>(null) }
    val destinationFlow = selectedDepartAirport?.let { viewModel.getDestinations(it.iata_code) }

    Scaffold(modifier = modifier.fillMaxSize()) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = viewModel::updateSearchText,
                label = { Text("输入机场名称/IATA代码") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                when {
                    searchText.isNotBlank() && autoCompleteItems.isNotEmpty() -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(autoCompleteItems) { airport ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedDepartAirport = airport }
                                        .padding(12.dp)
                                ) {
                                    Text(airport.iata_code)
                                    Text(airport.name)
                                }
                            }
                        }
                    }

                    searchText.isBlank() && allFavorites.isNotEmpty() -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(allFavorites) { favItem ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text("${favItem.depIata} ${favItem.depName} → ${favItem.destIata} ${favItem.destName}")
                                    IconButton(onClick = {
                                        viewModel.removeFav(favItem.depIata, favItem.destIata)
                                    }) {
                                        Icon(Icons.Filled.Favorite, null)
                                    }
                                }
                            }
                        }
                    }

                    selectedDepartAirport != null && destinationFlow != null -> {
                        val destAirports by destinationFlow.collectAsStateWithLifecycle(emptyList())
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(destAirports) { destAirport ->
                                val depAirport = selectedDepartAirport!!
                                val favCount by viewModel.checkIsFav(depAirport.iata_code, destAirport.iata_code)
                                    .collectAsStateWithLifecycle(0)

                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Text("出发：${depAirport.iata_code} ${depAirport.name}")
                                    Text("到达：${destAirport.iata_code} ${destAirport.name}")
                                    IconButton(onClick = {
                                        if (favCount > 0) viewModel.removeFav(depAirport.iata_code, destAirport.iata_code)
                                        else viewModel.addFav(depAirport.iata_code, destAirport.iata_code)
                                    }) {
                                        if (favCount > 0) Icon(Icons.Filled.Favorite, null)
                                        else Icon(Icons.Outlined.FavoriteBorder, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}