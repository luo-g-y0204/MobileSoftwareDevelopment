package com.example.flightsearch.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.FlightDatabase
import com.example.flightsearch.data.UserPreferencesRepository
import com.example.flightsearch.data.entity.Airport
import com.example.flightsearch.data.entity.FavoriteWithNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlightViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = FlightDatabase.getInstance(application).flightDao()
    private val prefsRepo = UserPreferencesRepository(application)

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedAirport = MutableStateFlow<Airport?>(null)
    val selectedAirport: StateFlow<Airport?> = _selectedAirport.asStateFlow()

    // 收藏列表改为手动刷新，移除无限轮询
    private val _favorites = MutableStateFlow<List<FavoriteWithNames>>(emptyList())
    val favorites: StateFlow<List<FavoriteWithNames>> = _favorites.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val suggestions: StateFlow<List<Airport>> = _searchText
        .flatMapLatest { query ->
            flow {
                if (query.isBlank()) emit(emptyList())
                else {
                    val list = withContext(Dispatchers.IO) {
                        dao.searchAirports(query)
                    }
                    emit(list)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val flights: StateFlow<List<Airport>> = _selectedAirport
        .flatMapLatest { airport ->
            flow {
                if (airport != null) {
                    val list = withContext(Dispatchers.IO) {
                        dao.getDestinations(airport.iataCode)
                    }
                    emit(list)
                } else {
                    emit(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 初始化加载一次收藏列表
        viewModelScope.launch {
            refreshFavorites()
        }

        // 恢复保存的搜索文本
        viewModelScope.launch {
            prefsRepo.searchText.collect { savedText ->
                if (_searchText.value != savedText) {
                    _searchText.value = savedText
                }
            }
        }
    }

    private suspend fun refreshFavorites() {
        withContext(Dispatchers.IO) {
            _favorites.value = dao.getFavoritesWithNames()
        }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
        viewModelScope.launch {
            prefsRepo.saveSearchText(text)
        }
        if (_selectedAirport.value != null) {
            _selectedAirport.value = null
        }
    }

    fun onAirportSelected(airport: Airport) {
        _selectedAirport.value = airport
        _searchText.value = airport.name
        viewModelScope.launch {
            prefsRepo.saveSearchText(airport.name)
        }
    }

    fun clearSelection() {
        _selectedAirport.value = null
    }

    fun toggleFavorite(departureCode: String, destinationCode: String, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (isCurrentlyFavorite) {
                    dao.removeFavorite(departureCode, destinationCode)
                } else {
                    dao.addFavorite(departureCode, destinationCode)
                }
            }
            refreshFavorites() // 操作完成后刷新列表
        }
    }
}