package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    @Query("SELECT * FROM airport ORDER BY passengers DESC")
    fun getAllAirports(): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE UPPER(iata_code) LIKE UPPER(:searchQuery) OR UPPER(name) LIKE UPPER(:searchQuery) ORDER BY passengers DESC")
    fun searchAirports(searchQuery: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code != :departureCode ORDER BY passengers DESC")
    fun getDestinations(departureCode: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code = :code")
    suspend fun getAirportByCode(code: String): Airport?

    @Query("SELECT * FROM favorite")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Query("SELECT f.*, d.name as departure_name, dest.name as destination_name FROM favorite f INNER JOIN airport d ON f.departure_code = d.iata_code INNER JOIN airport dest ON f.destination_code = dest.iata_code")
    fun getFavoriteWithAirports(): Flow<List<FavoriteWithAirports>>

    @Query("SELECT COUNT(*) FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
    suspend fun isFavorite(departureCode: String, destinationCode: String): Int

    @Query("SELECT * FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
    suspend fun getFavorite(departureCode: String, destinationCode: String): Favorite?

    @Insert
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)
}

data class FavoriteWithAirports(
    val id: Int,
    val departure_code: String,
    val destination_code: String,
    val departure_name: String,
    val destination_name: String
)