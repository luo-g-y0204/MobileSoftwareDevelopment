package com.example.flightsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FavoriteWithAirports
import com.example.flightsearch.data.FlightDatabase
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FlightViewModel(
    private val database: FlightDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedAirport = MutableStateFlow<Airport?>(null)
    val selectedAirport: StateFlow<Airport?> = _selectedAirport.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Airport>>(emptyList())
    val searchResults: StateFlow<List<Airport>> = _searchResults.asStateFlow()

    private val _destinations = MutableStateFlow<List<Airport>>(emptyList())
    val destinations: StateFlow<List<Airport>> = _destinations.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteWithAirports>>(emptyList())
    val favorites: StateFlow<List<FavoriteWithAirports>> = _favorites.asStateFlow()

    private val _isFavoriteMap = mutableMapOf<String, Boolean>()

    init {
        viewModelScope.launch {
            userPreferencesRepository.searchTextFlow.collectLatest { savedText ->
                _searchText.value = savedText
                if (savedText.isNotEmpty()) {
                    performSearch(savedText)
                }
            }
        }

        viewModelScope.launch {
            database.flightDao().getFavoriteWithAirports().collectLatest {
                _favorites.value = it
                updateFavoriteMap(it)
            }
        }
    }

    fun updateSearchText(text: String) {
        _searchText.value = text
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.saveSearchText(text)
        }
        performSearch(text)
    }

    private fun performSearch(text: String) {
        if (text.isEmpty()) {
            _searchResults.value = emptyList()
            _selectedAirport.value = null
            _destinations.value = emptyList()
        } else {
            val query = "%${text}%"
            viewModelScope.launch(Dispatchers.IO) {
                database.flightDao().searchAirports(query).collectLatest {
                    _searchResults.value = it
                }
            }
        }
    }

    fun selectAirport(airport: Airport) {
        _selectedAirport.value = airport
        _searchResults.value = emptyList()
        _searchText.value = airport.iata_code

        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.saveSearchText(airport.iata_code)
            database.flightDao().getDestinations(airport.iata_code).collectLatest {
                _destinations.value = it
            }
        }
    }

    fun isFavorite(departureCode: String, destinationCode: String): Boolean {
        return _isFavoriteMap["$departureCode-$destinationCode"] ?: false
    }

    fun toggleFavorite(departureCode: String, destinationCode: String) {
        val key = "$departureCode-$destinationCode"
        val isFavorite = _isFavoriteMap[key] ?: false

        viewModelScope.launch(Dispatchers.IO) {
            if (isFavorite) {
                val favorite = database.flightDao().getFavorite(departureCode, destinationCode)
                favorite?.let { fav ->
                    database.flightDao().deleteFavorite(fav)
                }
            } else {
                database.flightDao().insertFavorite(
                    Favorite(departure_code = departureCode, destination_code = destinationCode)
                )
            }
        }
    }

    private fun updateFavoriteMap(favorites: List<FavoriteWithAirports>) {
        _isFavoriteMap.clear()
        favorites.forEach {
            _isFavoriteMap["${it.departure_code}-${it.destination_code}"] = true
        }
    }
}

class FlightViewModelFactory(
    private val database: FlightDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(FlightViewModel::class.java)) {
            return FlightViewModel(database, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}