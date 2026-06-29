package com.example.flightsearch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * UI 状态密封类
 * 表示应用的三种不同界面状态
 */
sealed class FlightUiState {
    /** 收藏列表界面 - 搜索框为空时显示 */
    data object FavoritesList : FlightUiState()

    /** 自动补全界面 - 用户正在输入时显示 */
    data object Autocomplete : FlightUiState()

    /** 航班列表界面 - 用户选择了机场后显示 */
    data class FlightList(val departureAirport: Airport) : FlightUiState()
}

/**
 * 航班搜索应用的 ViewModel
 * 管理所有 UI 状态和业务逻辑
 */
class FlightViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FlightDatabase.getDatabase(application)
    private val dao = database.flightDao()
    private val preferencesRepository = UserPreferencesRepository.getInstance(application)

    // ==================== 搜索文本 ====================

    /** 搜索文本状态 */
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    /** 防抖搜索文本 - 用于触发自动补全查询，减少数据库查询频率 */
    private val _debouncedSearchText = MutableStateFlow("")

    // ==================== 界面状态 ====================

    /** 当前 UI 状态 */
    private val _uiState = MutableStateFlow<FlightUiState>(FlightUiState.FavoritesList)
    val uiState: StateFlow<FlightUiState> = _uiState.asStateFlow()

    // ==================== 数据流 ====================

    /** 自动补全建议列表 - 根据防抖后的搜索文本查询数据库 */
    val autocompleteSuggestions = _debouncedSearchText.let { debouncedFlow ->
        combine(debouncedFlow, MutableStateFlow(Unit)) { query, _ ->
            query
        }.let { combinedFlow ->
            // 通过 switchMap 风格的转换实现
            MutableStateFlow<List<Airport>>(emptyList()).also { resultFlow ->
                viewModelScope.launch {
                    combinedFlow.collect { query ->
                        if (query.isBlank()) {
                            resultFlow.value = emptyList()
                        } else {
                            dao.searchAirports("%$query%").collect { results ->
                                resultFlow.value = results
                            }
                        }
                    }
                }
            }
        }
    }

    /** 当前选择的出发机场 */
    private val _selectedAirport = MutableStateFlow<Airport?>(null)
    val selectedAirport: StateFlow<Airport?> = _selectedAirport.asStateFlow()

    /** 航班列表 - 从当前选中的机场出发的所有目的地 */
    private val _destinations = MutableStateFlow<List<Airport>>(emptyList())
    val destinations: StateFlow<List<Airport>> = _destinations.asStateFlow()

    /** 收藏航线列表 */
    val favorites: StateFlow<List<FavoriteWithAirports>> = dao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 已收藏的航线代码集合（用于快速判断收藏状态） */
    private val _favoriteSet = MutableStateFlow<Set<String>>(emptySet())
    val favoriteSet: StateFlow<Set<String>> = _favoriteSet.asStateFlow()

    /** 搜索文本变化监听任务 */
    private var searchJob: Job? = null

    init {
        // 从 DataStore 恢复搜索文本
        viewModelScope.launch {
            val savedText = preferencesRepository.searchTextFlow.first()
            _searchText.value = savedText

            if (savedText.isNotBlank()) {
                // 如果保存的搜索文本不为空，触发搜索
                updateDebouncedSearch(savedText)
                _uiState.value = FlightUiState.Autocomplete
            } else {
                // 搜索文本为空，显示收藏列表
                _uiState.value = FlightUiState.FavoritesList
            }
        }

        // 监听收藏变化，更新收藏集合
        viewModelScope.launch {
            favorites.collect { favoriteList ->
                _favoriteSet.value = favoriteList.map {
                    "${it.departureCode}_${it.destinationCode}"
                }.toSet()
            }
        }
    }

    // ==================== 搜索方法 ====================

    /**
     * 更新搜索文本
     * 带防抖功能，避免每次按键都查询数据库
     */
    fun onSearchTextChanged(text: String) {
        _searchText.value = text

        // 取消之前的防抖任务
        searchJob?.cancel()

        if (text.isBlank()) {
            // 搜索文本为空 -> 显示收藏列表
            _debouncedSearchText.value = ""
            _selectedAirport.value = null
            _uiState.value = FlightUiState.FavoritesList
            return
        }

        // 启动防抖，300ms 后执行搜索
        searchJob = viewModelScope.launch {
            delay(300)
            updateDebouncedSearch(text)
            _uiState.value = FlightUiState.Autocomplete
        }
    }

    /**
     * 更新防抖搜索文本
     */
    private fun updateDebouncedSearch(text: String) {
        _debouncedSearchText.value = text
    }

    /**
     * 选择机场
     * 用户点击自动补全建议后，查询该机场出发的航班列表
     */
    fun onAirportSelected(airport: Airport) {
        _searchText.value = "${airport.name} (${airport.iataCode})"
        _selectedAirport.value = airport
        _debouncedSearchText.value = ""

        // 查询从该机场出发的所有目的地
        viewModelScope.launch {
            dao.getDestinations(airport.iataCode).collect { destinationList ->
                _destinations.value = destinationList
            }
        }

        _uiState.value = FlightUiState.FlightList(airport)
        // 保存搜索文本
        saveCurrentSearchText()
    }

    /**
     * 清除搜索
     * 回到收藏列表界面
     */
    fun onClearSearch() {
        _searchText.value = ""
        _debouncedSearchText.value = ""
        _selectedAirport.value = null
        _uiState.value = FlightUiState.FavoritesList
        viewModelScope.launch {
            preferencesRepository.saveSearchText("")
        }
    }

    // ==================== 收藏方法 ====================

    /**
     * 切换收藏状态
     * 如果已收藏则删除，否则添加
     */
    fun toggleFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch {
            val count = dao.isFavorite(departureCode, destinationCode)
            if (count > 0) {
                dao.deleteFavorite(departureCode, destinationCode)
            } else {
                dao.addFavorite(Favorite(departureCode = departureCode, destinationCode = destinationCode))
            }
        }
    }

    /**
     * 检查某条航线是否已收藏
     */
    fun checkIsFavorite(departureCode: String, destinationCode: String): Boolean {
        val key = "${departureCode}_${destinationCode}"
        return _favoriteSet.value.contains(key)
    }

    // ==================== 持久化方法 ====================

    /**
     * 保存当前搜索文本到 DataStore
     */
    fun saveCurrentSearchText() {
        viewModelScope.launch {
            preferencesRepository.saveSearchText(_searchText.value)
        }
    }

    /**
     * 获取自动补全建议（供 UI 层调用）
     */
    fun getAutocompleteSuggestions() = dao.searchAirports("%${_debouncedSearchText.value}%")
}
