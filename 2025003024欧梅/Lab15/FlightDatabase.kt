package com.example.flightsearchlab15.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [Airport::class, Favorite::class],
    version = 1,
    exportSchema = false
)
abstract class FlightDatabase : RoomDatabase() {
    abstract fun flightDao(): FlightDao

    companion object {
        // 单例，从assets读取预置flight_search.db
        fun getInstance(context: Context): FlightDatabase {
            return Room.databaseBuilder(
                context,
                FlightDatabase::class.java,
                "flight_db"
            )
                .createFromAsset("database/flight_search.db")
                .build()
        }
    }
}