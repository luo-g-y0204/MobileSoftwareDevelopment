package com.example.flightsearch.data.entity

data class Airport(
    val id: Int,
    val iataCode: String,
    val name: String,
    val passengers: Int
) {
    companion object {
        const val TABLE_NAME = "airport"
        const val COLUMN_ID = "id"
        const val COLUMN_IATA_CODE = "iata_code"
        const val COLUMN_NAME = "name"
        const val COLUMN_PASSENGERS = "passengers"
    }

    val fullDisplayName: String
        get() = "${name}（${iataCode}）"

    val passengerSummary: String
        get() = "旅客吞吐量：${passengers} 人次/年"
}