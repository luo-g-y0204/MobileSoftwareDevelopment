package com.example.flightsearch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// 收藏数据库实体
@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val departure_code: String,
    val destination_code: String
)

// 收藏联表查询结果DTO（携带完整机场名称）
data class FavoriteRoute(
    val favoriteId: Int,
    val departureCode: String,
    val departureName: String,
    val destinationCode: String,
    val destinationName: String
)

// 单条航班数据+收藏状态，UI渲染专用
data class FlightRoute(
    val departAirport: Airport,
    val destAirport: Airport,
    val isFavorite: Boolean
)