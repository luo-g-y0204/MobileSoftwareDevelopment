package com.example.flightsearch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {

    /** 实验要求中的“查询所有机场”，按客流量由高到低。 */
    @Query("SELECT * FROM airport ORDER BY passengers DESC")
    fun observeAllAirports(): Flow<List<Airport>>

    /**
     * 自动补全：同时匹配 IATA 代码与机场名称。
     * LIMIT 避免输入一个字母时一次性读取整个数据库。
     */
    @Query(
        """
        SELECT * FROM airport
        WHERE iata_code LIKE '%' || :query || '%' COLLATE NOCASE
           OR name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY passengers DESC
        LIMIT 10
        """
    )
    fun searchAirports(query: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code = :iataCode COLLATE NOCASE LIMIT 1")
    suspend fun getAirportByCode(iataCode: String): Airport?

    /**
     * 假设每个机场都能飞往其他所有机场，因此使用 CROSS JOIN 生成目的地，
     * 并排除出发机场自身。
     */
    @Query(
        """
        SELECT
            departure.iata_code AS departure_code,
            departure.name AS departure_name,
            destination.iata_code AS destination_code,
            destination.name AS destination_name
        FROM airport AS departure
        CROSS JOIN airport AS destination
        WHERE departure.iata_code = :departureCode COLLATE NOCASE
          AND destination.iata_code != :departureCode COLLATE NOCASE
        ORDER BY destination.passengers DESC
        """
    )
    fun observeFlightsFrom(departureCode: String): Flow<List<FlightRoute>>

    /** 收藏表只保存代码，因此通过两次 INNER JOIN 补全机场名称。 */
    @Query(
        """
        SELECT
            favorite.id AS id,
            favorite.departure_code AS departure_code,
            departure.name AS departure_name,
            favorite.destination_code AS destination_code,
            destination.name AS destination_name
        FROM favorite
        INNER JOIN airport AS departure
            ON favorite.departure_code = departure.iata_code
        INNER JOIN airport AS destination
            ON favorite.destination_code = destination.iata_code
        ORDER BY departure.iata_code, destination.iata_code
        """
    )
    fun observeFavoriteRoutes(): Flow<List<FavoriteRoute>>

    @Query("SELECT * FROM favorite ORDER BY id")
    fun observeFavorites(): Flow<List<Favorite>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM favorite
            WHERE departure_code = :departureCode
              AND destination_code = :destinationCode
        )
        """
    )
    suspend fun isFavorite(departureCode: String, destinationCode: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: Favorite): Long

    @Query(
        """
        DELETE FROM favorite
        WHERE departure_code = :departureCode
          AND destination_code = :destinationCode
        """
    )
    suspend fun deleteFavorite(departureCode: String, destinationCode: String)
}
