package com.example.flightsearch

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 航班数据访问对象
 * 定义所有数据库操作接口
 */
@Dao
interface FlightDao {

    /**
     * 自动补全查询
     * 根据用户输入搜索机场名称或 IATA 代码
     * 使用 LIKE 关键字进行模糊匹配
     * 按乘客数量降序排列，优先显示更繁忙的机场
     *
     * @param query 搜索关键词（已添加 % 通配符）
     * @return 匹配的机场列表 Flow
     */
    @Query("""
        SELECT * FROM airport
        WHERE iata_code LIKE :query OR name LIKE :query
        ORDER BY passengers DESC
    """)
    fun searchAirports(query: String): Flow<List<Airport>>

    /**
     * 获取所有机场（用于初始化等场景）
     *
     * @return 所有机场列表 Flow
     */
    @Query("SELECT * FROM airport ORDER BY passengers DESC")
    fun getAllAirports(): Flow<List<Airport>>

    /**
     * 根据 IATA 代码获取单个机场
     *
     * @param iataCode IATA 代码
     * @return 对应的机场
     */
    @Query("SELECT * FROM airport WHERE iata_code = :iataCode LIMIT 1")
    suspend fun getAirportByCode(iataCode: String): Airport?

    /**
     * 航班查询
     * 获取从指定机场出发的所有目的地
     * 排除目的地与出发地相同的情况（飞机不飞回自身）
     * 假设每个机场都有飞往其他所有机场的航班
     *
     * @param departureCode 出发地 IATA 代码
     * @return 除自身外的所有机场列表 Flow
     */
    @Query("""
        SELECT * FROM airport
        WHERE iata_code != :departureCode
        ORDER BY passengers DESC
    """)
    fun getDestinations(departureCode: String): Flow<List<Airport>>

    /**
     * 收藏查询（联合查询）
     * 获取所有收藏航线，通过联合查询显示机场名称
     * 关联 airport 表两次以获取出发地和目的地的详细信息
     *
     * @return 收藏航线信息（包含出发地和目的地的完整机场信息）
     */
    @Query("""
        SELECT
            favorite.id AS favoriteId,
            favorite.departure_code AS departureCode,
            favorite.destination_code AS destinationCode,
            departureAirport.name AS departureName,
            departureAirport.passengers AS departurePassengers,
            destinationAirport.name AS destinationName,
            destinationAirport.passengers AS destinationPassengers
        FROM favorite
        INNER JOIN airport AS departureAirport
            ON favorite.departure_code = departureAirport.iata_code
        INNER JOIN airport AS destinationAirport
            ON favorite.destination_code = destinationAirport.iata_code
        ORDER BY destinationAirport.passengers DESC
    """)
    fun getAllFavorites(): Flow<List<FavoriteWithAirports>>

    /**
     * 添加收藏航线
     * 冲突策略为忽略（如果已存在相同的航线则跳过）
     *
     * @param favorite 收藏航线对象
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: Favorite)

    /**
     * 删除收藏航线
     * 根据出发地代码和目的地代码删除指定收藏
     *
     * @param departureCode 出发地 IATA 代码
     * @param destinationCode 目的地 IATA 代码
     */
    @Query("DELETE FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
    suspend fun deleteFavorite(departureCode: String, destinationCode: String)

    /**
     * 检查某条航线是否已被收藏
     *
     * @param departureCode 出发地 IATA 代码
     * @param destinationCode 目的地 IATA 代码
     * @return 是否存在
     */
    @Query("SELECT COUNT(*) FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
    suspend fun isFavorite(departureCode: String, destinationCode: String): Int
}

/**
 * 收藏航线与机场信息的联合查询结果
 * 用于在 UI 中展示收藏航线的详细信息
 */
data class FavoriteWithAirports(
    val favoriteId: Int,
    val departureCode: String,
    val destinationCode: String,
    val departureName: String,
    val departurePassengers: Int,
    val destinationName: String,
    val destinationPassengers: Int
)
