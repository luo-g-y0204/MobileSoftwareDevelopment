package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    // 自动补全：模糊匹配IATA/名称，客流量降序
    @Query("SELECT * FROM airport WHERE iata_code LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY passengers DESC")
    fun searchAirports(query: String): Flow<List<Airport>>

    // 查询出发机场所有目的地（排除自身）
    @Query("SELECT * FROM airport WHERE iata_code != :departIata ORDER BY passengers DESC")
    fun getDestinations(departIata: String): Flow<List<Airport>>

    // 联合查询所有收藏，带出机场名称
    @Query("""
        SELECT f.id, f.departure_code as departureCode, a1.name as departureName,
        f.destination_code as destinationCode, a2.name as destinationName
        FROM favorite f
        INNER JOIN airport a1 ON f.departure_code = a1.iata_code
        INNER JOIN airport a2 ON f.destination_code = a2.iata_code
    """)
    fun getAllFavoritesWithName(): Flow<List<FavoriteFlight>>

    // 根据两段代码查询单条收藏
    @Query("SELECT * FROM favorite WHERE departure_code = :dep AND destination_code = :dest LIMIT 1")
    suspend fun getFavoriteByRoute(dep: String, dest: String): Favorite?

    // 新增收藏
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(fav: Favorite)

    // 删除收藏
    @Delete
    suspend fun deleteFavorite(fav: Favorite)
}

// 联合查询返回封装类
data class FavoriteFlight(
    val id: Int,
    val departureCode: String,
    val departureName: String,
    val destinationCode: String,
    val destinationName: String
)