package com.example.flightsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlightUiState(
    val searchQuery: String = "",
    val selectedAirport: Airport? = null,
    val favorites: List<Favorite> = emptyList()
)

class FlightViewModel(
    private val flightDao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightUiState())
    val uiState: StateFlow<FlightUiState> = _uiState.asStateFlow()

    val allAirports: StateFlow<List<Airport>> = flightDao.getAllAirports()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val searchResults: StateFlow<List<Airport>> = _uiState
        .combine(flightDao.getAllAirports()) { state, allAirports ->
            if (state.searchQuery.isBlank()) emptyList()
            else allAirports.filter {
                it.iata_code.contains(state.searchQuery, ignoreCase = true) ||
                        it.name.contains(state.searchQuery, ignoreCase = true)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val destinations: StateFlow<List<Airport>> = _uiState
        .combine(flightDao.getAllAirports()) { state, allAirports ->
            state.selectedAirport?.let {
                allAirports.filter { it.iata_code != state.selectedAirport!!.iata_code }
            } ?: emptyList()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.lastSearchQuery.collect { lastQuery ->
                _uiState.update { it.copy(searchQuery = lastQuery) }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.selectedAirport.collect { selectedCode ->
                if (selectedCode.isNotEmpty()) {
                    val airport = flightDao.getAirportByCode(selectedCode)
                    _uiState.update { it.copy(selectedAirport = airport) }
                }
            }
        }

        viewModelScope.launch {
            flightDao.getAllFavorites().collect { favList ->
                _uiState.update { it.copy(favorites = favList) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.saveLastSearchQuery(query)
            _uiState.update {
                it.copy(searchQuery = query, selectedAirport = null)
            }
        }
    }

    fun selectAirport(airport: Airport) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.saveSelectedAirport(airport.iata_code)
            _uiState.update {
                it.copy(selectedAirport = airport, searchQuery = "")
            }
        }
    }

    fun clearSelection() {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.clearPreferences()
            _uiState.update { it.copy(selectedAirport = null) }
        }
    }

    fun toggleFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val exist = flightDao.getFavorite(departureCode, destinationCode)
            if (exist != null) {
                flightDao.deleteFavorite(departureCode, destinationCode)
            } else {
                flightDao.insertFavorite(
                    Favorite(
                        departure_code = departureCode,
                        destination_code = destinationCode
                    )
                )
            }
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