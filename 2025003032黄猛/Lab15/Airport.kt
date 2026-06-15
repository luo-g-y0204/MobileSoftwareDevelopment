package com.example.flightsearch.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "iata_code") val iataCode: String,
    val name: String,
    val passengers: Int,
) {
    val displayName: String
        get() = "$iataCode - $name"

    val passengerLabel: String
        get() = "%d passengers".format(passengers)

    fun matches(query: String): Boolean {
        if (query.isBlank()) return false
        return iataCode.contains(query, ignoreCase = true) ||
            name.contains(query, ignoreCase = true)
    }
}