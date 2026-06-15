package com.example.flightsearchlab15.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {

    // 1. 自动补全模糊搜索：iata_code/name 模糊匹配，按客流量降序
    @Query("""
        SELECT * FROM airport 
        WHERE iata_code LIKE :key OR name LIKE :key 
        ORDER BY passengers DESC
    """)
    fun searchAirport(key: String): Flow<List<Airport>>

    // 2. 查询指定出发机场的所有其他机场（航班目的地，排除自身）
    @Query("SELECT * FROM airport WHERE iata_code != :departIata")
    fun getDestinations(departIata: String): Flow<List<Airport>>

    // 3. 收藏联查：favorite + 两张airport表关联，获取完整航线名称
    @Query("""
        SELECT f.id favoriteId,
               d.iata_code departCode, d.name departName,
               t.iata_code destCode, t.name destName
        FROM favorite f
        INNER JOIN airport d ON f.departure_code = d.iata_code
        INNER JOIN airport t ON f.destination_code = t.iata_code
    """)
    fun getAllFavorites(): Flow<List<FavoriteRoute>>

    // 4. 判断某条航线是否已收藏
    @Query("""
        SELECT COUNT(*) FROM favorite 
        WHERE departure_code = :dep AND destination_code = :dest
    """)
    fun isRouteFavorite(dep: String, dest: String): Flow<Int>

    // 新增收藏
    @Insert
    suspend fun addFavorite(fav: Favorite)

    // 删除收藏
    @Delete
    suspend fun removeFavorite(fav: Favorite)
}