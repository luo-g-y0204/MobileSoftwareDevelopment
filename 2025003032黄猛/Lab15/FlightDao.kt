package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    @Query("SELECT * FROM airport ORDER BY passengers DESC, iata_code ASC")
    fun observeAllAirports(): Flow<List<Airport>>

    @Query(
        """
        SELECT * FROM airport
        WHERE iata_code LIKE :searchQuery OR name LIKE :searchQuery
        ORDER BY passengers DESC, iata_code ASC
        LIMIT 10
        """
    )
    fun searchAirports(searchQuery: String): Flow<List<Airport>>

    @Query(
        """
        SELECT
            departure.id AS departure_id,
            departure.iata_code AS departure_iata_code,
            departure.name AS departure_name,
            departure.passengers AS departure_passengers,
            destination.id AS destination_id,
            destination.iata_code AS destination_iata_code,
            destination.name AS destination_name,
            destination.passengers AS destination_passengers
        FROM airport AS departure
        CROSS JOIN airport AS destination
        WHERE departure.iata_code = :departureCode
          AND destination.iata_code != :departureCode
        ORDER BY destination.passengers DESC, destination.iata_code ASC
        """
    )
    fun observeRoutesFrom(departureCode: String): Flow<List<AirportRoute>>

    @Query(
        """
        SELECT
            departure.id AS departure_id,
            departure.iata_code AS departure_iata_code,
            departure.name AS departure_name,
            departure.passengers AS departure_passengers,
            destination.id AS destination_id,
            destination.iata_code AS destination_iata_code,
            destination.name AS destination_name,
            destination.passengers AS destination_passengers
        FROM favorite AS favorite_route
        INNER JOIN airport AS departure
            ON favorite_route.departure_code = departure.iata_code
        INNER JOIN airport AS destination
            ON favorite_route.destination_code = destination.iata_code
        ORDER BY departure.passengers DESC, destination.passengers DESC
        """
    )
    fun observeFavoriteRoutes(): Flow<List<AirportRoute>>

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