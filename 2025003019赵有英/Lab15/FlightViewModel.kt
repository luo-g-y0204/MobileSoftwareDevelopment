package com.example.flightsearch.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.StateFlow
import androidx.lifecycle.MutableStateFlow
import androidx.lifecycle.stateIn
import androidx.lifecycle.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FavoriteFlight
import com.example.flightsearch.data.FlightDatabase
import com.example.flightsearch.data.UserPreferencesRepository

class FlightViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = FlightDatabase.getInstance(app).flightDao()
    private val prefsRepo = UserPreferencesRepository(app)

    // 界面搜索文本状态
    var searchText by mutableStateOf("")
        private set

    // 选中的出发机场
    var selectedDepartAirport: Airport? by mutableStateOf(null)
        private set

    // 自动补全机场列表流
    val airportSuggestions: StateFlow<List<Airport>> = snapshotFlow { searchText }
        .flatMapLatest { input ->
            if (input.isBlank()) flowOf(emptyList())
            else dao.searchAirports(input)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 选中机场后的目的地航班列表
    val destinationFlights: StateFlow<List<Airport>> = snapshotFlow { selectedDepartAirport }
        .flatMapLatest { airport ->
            airport?.let { dao.getDestinations(it.iataCode) } ?: flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 全部收藏航班
    val allFavoriteFlights: StateFlow<List<FavoriteFlight>> = dao.getAllFavoritesWithName()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 快速判断收藏集合
    private val favoriteRouteSet = MutableStateFlow<Set<String>>(emptySet())

    init {
        // 初始化加载收藏标识
        viewModelScope.launch {
            allFavoriteFlights.collect { favList ->
                favoriteRouteSet.value = favList.map { "${it.departureCode}_${it.destinationCode}" }.toSet()
            }
        }
        // 初始化读取上次保存的搜索文字
        viewModelScope.launch {
            prefsRepo.savedSearchTextFlow.collect { text ->
                searchText = text
                if (text.isBlank()) selectedDepartAirport = null
            }
        }
    }

    // 更新搜索框文字并持久化
    fun updateSearchText(newText: String) {
        searchText = newText
        viewModelScope.launch { prefsRepo.saveSearchText(newText) }
        if (newText.isBlank()) selectedDepartAirport = null
    }

    // 选中一个出发机场
    fun selectDepartAirport(airport: Airport) {
        selectedDepartAirport = airport
        searchText = airport.name
        viewModelScope.launch { prefsRepo.saveSearchText(airport.name) }
    }

    // 判断航线是否已收藏
    fun isRouteFavorite(depCode: String, destCode: String): Boolean {
        return "${depCode}_${destCode}" in favoriteRouteSet.value
    }

    // 添加收藏
    fun addFavorite(depCode: String, destCode: String) {
        viewModelScope.launch {
            dao.insertFavorite(Favorite(departureCode = depCode, destinationCode = destCode))
        }
    }

    // 删除收藏
    fun removeFavorite(depCode: String, destCode: String) {
        viewModelScope.launch {
            dao.getFavoriteByRoute(depCode, destCode)?.let { dao.deleteFavorite(it) }
        }
    }
}