package com.example.flightsearchlab15.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey val id: Int = 0,
    val departure_code: String,
    val destination_code: String
)

// 多表联查返回封装：收藏+出发机场+目的地机场完整信息
data class FavoriteRoute(
    val favoriteId: Int,
    val departCode: String,
    val departName: String,
    val destCode: String,
    val destName: String
)