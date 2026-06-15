package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    // 自动补全：模糊匹配IATA/机场名称，按客流量降序
    @Query("SELECT * FROM airport WHERE iata_code LIKE :keyword OR name LIKE :keyword ORDER BY passengers DESC")
    fun searchAirports(keyword: String): Flow<List<Airport>>

    // 查询指定出发机场所有其他目的地（排除自身）
    @Query("SELECT * FROM airport WHERE iata_code != :departCode")
    fun getAllDestinations(departCode: String): Flow<List<Airport>>

    // 收藏联表查询，JOIN两次airport表获取完整名称
    @Query("""
        SELECT f.id as favoriteId,
               f.departure_code as departureCode,
               dep.name as departureName,
               f.destination_code as destinationCode,
               dest.name as destinationName
        FROM favorite f
        INNER JOIN airport dep ON f.departure_code = dep.iata_code
        INNER JOIN airport dest ON f.destination_code = dest.iata_code
    """)
    fun getAllFavorites(): Flow<List<FavoriteRoute>>

    // 判断航线是否已收藏
    @Query("SELECT COUNT(*) FROM favorite WHERE departure_code = :dep AND destination_code = :dest")
    fun isRouteFavorite(dep: String, dest: String): Flow<Int>

    // 新增收藏
    @Insert
    suspend fun addFavorite(favorite: Favorite)

    // 删除收藏
    @Delete
    suspend fun removeFavorite(favorite: Favorite)

    // 根据起终点查询单条收藏记录
    @Query("SELECT * FROM favorite WHERE departure_code = :dep AND destination_code = :dest LIMIT 1")
    suspend fun getFavoriteByRoute(dep: String, dest: String): Favorite?
}