package com.example.busschedule.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.busschedule.data.BusSchedule
import com.example.busschedule.data.BusScheduleDao
import com.example.busschedule.data.BusScheduleDatabase
import kotlinx.coroutines.flow.Flow

class BusScheduleViewModel(
    private val busScheduleDao: BusScheduleDao
) : ViewModel() {

    fun getFullSchedule(): Flow<List<BusSchedule>> = busScheduleDao.getAll()

    fun getScheduleFor(stopName: String): Flow<List<BusSchedule>> =
        busScheduleDao.getByStopName(stopName)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val database = BusScheduleDatabase.getDatabase(application)
                BusScheduleViewModel(database.busScheduleDao())
            }
        }
    }
}