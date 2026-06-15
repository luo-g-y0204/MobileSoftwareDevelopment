package com.example.flightsearch.data.entity

data class Favorite(
    val id: Int = 0,
    val departureCode: String,
    val destinationCode: String
)
/**
 * 收藏航线实体类
 * 对应数据库 favorite 表的单条收藏记录
 *
 * @property id 收藏记录的唯一主键，自增整型
 * @property departureCode 出发机场的 IATA 三字代码
 * @property destinationCode 目的地机场的 IATA 三字代码
 * 表内对出发+目的地组合设置了唯一约束，避免重复收藏同一条航线
 */

data class FavoriteWithNames(
    val id: Int,
    val departureCode: String,
    val destinationCode: String,
    val departureName: String,
    val destinationName: String
)