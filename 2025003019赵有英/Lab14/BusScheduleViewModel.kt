package com.example.busschedule.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.busschedule.data.BusSchedule
import com.example.busschedule.data.BusScheduleDao
import com.example.busschedule.data.BusScheduleDatabase
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
class BusScheduleViewModel(
    private val busScheduleDao: BusScheduleDao
) : ViewModel() {

    fun getFullSchedule(): Flow<List<BusSchedule>> = busScheduleDao.getAll()

    fun getScheduleFor(stopName: String): Flow<List<BusSchedule>> =
        busScheduleDao.getByStopName(stopName)

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val database = BusScheduleDatabase.getDatabase(application)
                BusScheduleViewModel(database.busScheduleDao())
            }
        }
    }
}