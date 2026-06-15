package com.example.flightsearch.data.entity

/**
 * 机场信息实体类
 * 对应数据库 airport 表的单条数据记录
 *
 * @property id 机场数据的唯一主键，自增整型
 * @property iataCode 机场的三字 IATA 国际代码，全局唯一
 * @property name 机场的官方全称
 * @property passengers 机场的年旅客吞吐量，用于搜索结果排序
 */
data class Airport(
    val id: Int,
    val iataCode: String,
    val name: String,
    val passengers: Int
)