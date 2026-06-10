package com.example.busschedule.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.busschedule.data.BusSchedule
import com.example.busschedule.data.BusScheduleDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BusScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BusScheduleDatabase.getDatabase(application)
    private val dao = database.busScheduleDao()

    fun getFullSchedule(): Flow<List<BusSchedule>> {
        return dao.getAllSchedules()
    }

    fun getScheduleFor(stopName: String): Flow<List<BusSchedule>> {
        return dao.getScheduleForStopName(stopName)
    }

    companion object {
        fun Factory(application: Application): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return BusScheduleViewModel(application) as T
                }
            }
        }
    }
}