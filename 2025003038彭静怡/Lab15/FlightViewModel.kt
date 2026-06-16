package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.data.FlightRepository
import com.example.flightsearch.data.UserPreferencesRepository
import com.example.flightsearch.data.local.Airport
import com.example.flightsearch.data.local.FavoriteRoute
import com.example.flightsearch.data.local.FlightRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/** UI 使用的航线模型，额外包含收藏状态。 */
data class FlightItem(
    val departureCode: String,
    val departureName: String,
    val destinationCode: String,
    val destinationName: String,
    val isFavorite: Boolean,
)

data class FlightUiState(
    val searchText: String = "",
    val selectedAirport: Airport? = null,
    val suggestions: List<Airport> = emptyList(),
    val flights: List<FlightItem> = emptyList(),
    val favorites: List<FavoriteRoute> = emptyList(),
    val isLoading: Boolean = true,
)

private data class FlightContentState(
    val suggestions: List<Airport>,
    val flights: List<FlightItem>,
    val favorites: List<FavoriteRoute>,
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FlightViewModel(
    private val flightRepository: FlightRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val searchText = MutableStateFlow("")
    private val selectedAirport = MutableStateFlow<Airport?>(null)
    private val initialized = MutableStateFlow(false)

    private val suggestions: Flow<List<Airport>> =
        combine(searchText.debounce(250).distinctUntilChanged(), selectedAirport) { text, selected ->
            text.trim() to selected
        }.flatMapLatest { (text, selected) ->
            when {
                text.isBlank() -> flowOf(emptyList())
                selected != null -> flowOf(emptyList())
                else -> flightRepository.searchAirports(text)
            }
        }

    private val favoriteKeys: Flow<Set<Pair<String, String>>> =
        flightRepository.observeFavorites().map { favorites ->
            favorites.map { it.departureCode to it.destinationCode }.toSet()
        }

    private val flights: Flow<List<FlightItem>> =
        selectedAirport.flatMapLatest { airport ->
            if (airport == null) {
                flowOf(emptyList())
            } else {
                combine(
                    flightRepository.observeFlightsFrom(airport.iataCode),
                    favoriteKeys,
                ) { routes: List<FlightRoute>, keys ->
                    routes.map { route ->
                        FlightItem(
                            departureCode = route.departureCode,
                            departureName = route.departureName,
                            destinationCode = route.destinationCode,
                            destinationName = route.destinationName,
                            isFavorite = (route.departureCode to route.destinationCode) in keys,
                        )
                    }
                }
            }
        }

    private val contentState: Flow<FlightContentState> =
        combine(
            suggestions,
            flights,
            flightRepository.observeFavoriteRoutes(),
        ) { suggestionList, flightList, favoriteList ->
            FlightContentState(
                suggestions = suggestionList,
                flights = flightList,
                favorites = favoriteList,
            )
        }

    val uiState = combine(
        searchText,
        selectedAirport,
        initialized,
        contentState,
    ) { text, selected, isInitialized, content ->
        FlightUiState(
            searchText = text,
            selectedAirport = selected,
            suggestions = content.suggestions,
            flights = content.flights,
            favorites = content.favorites,
            isLoading = !isInitialized,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FlightUiState(),
    )

    init {
        restoreSearchText()
    }

    private fun restoreSearchText() {
        viewModelScope.launch {
            val restoredText = preferencesRepository.searchTextFlow.first()
            searchText.value = restoredText

            // 若保存的是完整 IATA 代码，重启后直接恢复对应航班列表；
            // 若只是未输入完的文字，则恢复文字并继续显示自动补全。
            if (restoredText.trim().length == 3) {
                selectedAirport.value =
                    flightRepository.getAirportByCode(restoredText.trim().uppercase())
            }
            initialized.value = true
        }
    }

    fun updateSearchText(newText: String) {
        searchText.value = newText
        selectedAirport.value = null
        persistSearchText(newText)
    }

    fun selectAirport(airport: Airport) {
        selectedAirport.value = airport
        searchText.value = airport.iataCode
        persistSearchText(airport.iataCode)
    }

    fun clearSearch() {
        selectedAirport.value = null
        searchText.value = ""
        persistSearchText("")
    }

    fun toggleFavorite(flight: FlightItem) {
        viewModelScope.launch {
            if (flight.isFavorite) {
                flightRepository.removeFavorite(
                    flight.departureCode,
                    flight.destinationCode,
                )
            } else {
                flightRepository.addFavorite(
                    flight.departureCode,
                    flight.destinationCode,
                )
            }
        }
    }

    fun removeFavorite(route: FavoriteRoute) {
        viewModelScope.launch {
            flightRepository.removeFavorite(
                route.departureCode,
                route.destinationCode,
            )
        }
    }

    private fun persistSearchText(text: String) {
        viewModelScope.launch {
            preferencesRepository.saveSearchText(text)
        }
    }

    companion object {
        fun factory(
            flightRepository: FlightRepository,
            preferencesRepository: UserPreferencesRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FlightViewModel(
                    flightRepository = flightRepository,
                    preferencesRepository = preferencesRepository,
                )
            }
        }
    }
}
