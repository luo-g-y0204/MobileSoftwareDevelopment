package com.example.flightsearch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FavoriteRoute
import com.example.flightsearch.data.FlightDao
import com.example.flightsearch.data.FlightRoute
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlightViewModel(
    private val dao: FlightDao,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {
    // 搜索框文本状态
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    // 自动补全机场列表（输入防抖300ms，减少频繁数据库查询）
    val airportSuggestions: Flow<List<Airport>> = searchText
        .debounce(300)
        .flatMapLatest { input ->
            if (input.isBlank()) flowOf(emptyList())
            else dao.searchAirports("%$input%")
        }

    // 所有收藏航线
    val favoriteRoutes: Flow<List<FavoriteRoute>> = dao.getAllFavorites()

    // 当前选中的出发机场
    private val _selectedDepartAirport = MutableStateFlow<Airport?>(null)
    val selectedDepartAirport: StateFlow<Airport?> = _selectedDepartAirport

    // 当前出发机场对应的所有航班，携带每条航线收藏状态
    val flightList: Flow<List<FlightRoute>> = selectedDepartAirport
        .flatMapLatest { depart ->
            if (depart == null) flowOf(emptyList())
            else dao.getAllDestinations(depart.iata_code)
                .flatMapLatest { destAirports ->
                    combine(
                        destAirports.map { dest ->
                            dao.isRouteFavorite(depart.iata_code, dest.iata_code)
                                .map { count -> dest to (count > 0) }
                        }
                    ) { stateList ->
                        stateList.map { pair ->
                            FlightRoute(depart, pair.first, pair.second)
                        }
                    }
                }
        }

    // 初始化读取上次保存的搜索文本
    init {
        viewModelScope.launch {
            prefsRepo.savedSearchText.collect { savedText ->
                _searchText.value = savedText
                // 自动匹配第一条机场
                if (savedText.isNotBlank()) {
                    val matchAirports = dao.searchAirports("%$savedText%").first()
                    if (matchAirports.isNotEmpty()) {
                        _selectedDepartAirport.value = matchAirports.first()
                    }
                }
            }
        }
    }

    // 更新搜索文本并持久化保存
    fun updateSearchText(text: String) {
        _searchText.value = text
        viewModelScope.launch { prefsRepo.saveSearchText(text) }
        // 清空输入时取消选中机场
        if (text.isBlank()) _selectedDepartAirport.value = null
    }

    // 点击自动补全选中机场
    fun selectAirport(airport: Airport) {
        _selectedDepartAirport.value = airport
    }

    // 切换收藏/取消收藏
    fun toggleFavorite(departCode: String, destCode: String) {
        viewModelScope.launch {
            val existRecord = dao.getFavoriteByRoute(departCode, destCode)
            if (existRecord == null) {
                dao.addFavorite(Favorite(departure_code = departCode, destination_code = destCode))
            } else {
                dao.removeFavorite(existRecord)
            }
        }
    }
}