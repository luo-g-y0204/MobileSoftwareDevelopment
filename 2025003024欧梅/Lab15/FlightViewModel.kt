package com.example.flightsearchlab15.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.flightsearchlab15.data.Airport
import com.example.flightsearchlab15.data.Favorite
import com.example.flightsearchlab15.data.FavoriteRoute
import com.example.flightsearchlab15.data.FlightDao
import com.example.flightsearchlab15.data.UserPreferencesRepository

class FlightViewModel(
    private val dao: FlightDao,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel {
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    val autoCompleteList: Flow<List<Airport>> = searchText
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteRoutes: Flow<List<FavoriteRoute>> = dao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getDestinations(depCode: String): Flow<List<Airport>> {
        return dao.getDestinations(depCode)
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
        viewModelScope.launch {
            prefsRepo.saveSearch(newText)
        }
    }

    fun addFav(dep: String, dest: String) = viewModelScope.launch {
        dao.addFavorite(Favorite(departure_code = dep, destination_code = dest))
    }

    fun removeFav(dep: String, dest: String) = viewModelScope.launch {
        dao.removeFavorite(Favorite(departure_code = dep, destination_code = dest))
    }

    fun checkIsFav(dep: String, dest: String): Flow<Int> {
        return dao.isFavorite(dep, dest)
    }

    init {
        _searchText.value = prefsRepo.getSyncText()
    }
}