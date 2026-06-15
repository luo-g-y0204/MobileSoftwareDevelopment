package com.example.flightsearch.data.entity

data class Favorite(
    val id: Int,
    val departureCode: String,
    val destinationCode: String
) {
    companion object {
        const val TABLE_NAME = "favorite"
        const val COLUMN_ID = "id"
        const val COLUMN_DEPARTURE = "departure_code"
        const val COLUMN_DESTINATION = "destination_code"
    }

    val uniqueRouteKey: String
        get() = "${departureCode}_${destinationCode}"

    fun matchRoute(depCode: String, destCode: String): Boolean {
        return departureCode == depCode && destinationCode == destCode
    }
}