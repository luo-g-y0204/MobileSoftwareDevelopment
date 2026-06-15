package com.example.flightsearch

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val departure_code: String,
    val destination_code: String
)

// 联合查询结果实体（页面展示用）
data class FavoriteFlight(
    val id: Int,
    val departure_code: String,
    val destination_code: String,
    val departure_name: String,
    val destination_name: String
)