package com.example.flightsearch

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Favorite 实体类
 * 映射 flight_search.db 中的 favorite 表
 * 用于存储用户收藏的航线
 */
@Entity(
    tableName = "favorite",
    indices = [Index(value = ["departure_code", "destination_code"], unique = true)]
)
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "departure_code")
    val departureCode: String,

    @ColumnInfo(name = "destination_code")
    val destinationCode: String
)
