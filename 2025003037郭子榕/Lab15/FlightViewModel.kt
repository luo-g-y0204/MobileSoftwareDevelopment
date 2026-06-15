package com.example.flightsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlightUiState(
    val searchQuery: String = "",
    val searchResults: List<Airport> = emptyList(),
    val selectedAirport: Airport? = null,
    val destinations: List<Airport> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
    val isSearching: Boolean = false
)

class FlightViewModel(
    private val flightDao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedAirport = MutableStateFlow<Airport?>(null)

    val uiState: StateFlow<FlightUiState> = combine(
        _searchQuery,
        _selectedAirport,
        getAllFavorites()
    ) { query, selected, favorites ->
        FlightUiState(
            searchQuery = query,
            selectedAirport = selected,
            favorites = favorites
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FlightUiState()
    )

    val searchResults: StateFlow<List<Airport>> = _searchQuery
        .combine(getAllAirports()) { query, allAirports ->
            if (query.isBlank()) {
                emptyList()
            } else {
                val upperQuery = query.uppercase()
                allAirports.filter { airport ->
                    airport.iata_code.contains(upperQuery) ||
                    airport.name.contains(query, ignoreCase = true)
                }.sortedBy { it.name }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val destinations: StateFlow<List<Airport>> = _selectedAirport
        .combine(getAllAirports()) { selected, allAirports ->
            if (selected == null) {
                emptyList()
            } else {
                allAirports.filter { it.iata_code != selected.iata_code }
                    .sortedBy { it.name }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getAllAirports(): StateFlow<List<Airport>> {
        return flightDao.getAllAirports().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun getAllFavorites(): StateFlow<List<Favorite>> {
        return flightDao.getAllFavorites().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _selectedAirport.value = null
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.saveLastSearchQuery(query)
        }
    }

    fun selectAirport(airport: Airport) {
        _selectedAirport.value = airport
        _searchQuery.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.saveSelectedAirport(airport.iata_code)
        }
    }

    fun clearSelection() {
        _selectedAirport.value = null
    }

    fun toggleFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = flightDao.getFavorite(departureCode, destinationCode)
            if (existing != null) {
                flightDao.deleteFavorite(departureCode, destinationCode)
            } else {
                flightDao.insertFavorite(Favorite(departure_code = departureCode, destination_code = destinationCode))
            }
        }
    }

    fun isFavorite(departureCode: String, destinationCode: String): Boolean {
        return uiState.value.favorites.any {
            it.departure_code == departureCode && it.destination_code == destinationCode
        }
    }

    fun deleteFavorite(favorite: Favorite) {
        viewModelScope.launch(Dispatchers.IO) {
            flightDao.deleteFavoriteById(favorite.id)
        }
    }
}

class FlightViewModelFactory(
    private val flightDao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlightViewModel::class.java)) {
            return FlightViewModel(flightDao, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}