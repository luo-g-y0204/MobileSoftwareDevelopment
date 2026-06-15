package com.example.flightsearch

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val departure_code: String,
    val destination_code: String
    // 下面两行删除，预置数据库没有这两列，是崩溃根源
    // val departureName: String = "",
    // val destinationName: String = ""
)