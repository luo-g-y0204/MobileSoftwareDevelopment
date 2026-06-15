package com.example.flightsearch.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flightsearch.ui.FlightViewModel

class FlightViewModelFactory(
    private val flightDao: FlightDao,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FlightViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return FlightViewModel(flightDao, preferencesRepository) as T
    }
}