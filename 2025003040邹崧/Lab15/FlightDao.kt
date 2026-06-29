package com.example.flightsearch.data

import com.example.flightsearch.data.entity.Airport
import com.example.flightsearch.data.entity.FavoriteWithNames

class FlightDao(private val dbHelper: FlightDatabase) {

    fun searchAirports(query: String): List<Airport> {
        val db = dbHelper.readableDatabase
        return db.rawQuery(
            "SELECT * FROM airport WHERE iata_code LIKE ? OR name LIKE ? ORDER BY passengers DESC",
            arrayOf("%$query%", "%$query%")
        ).use { cursor ->
            val list = mutableListOf<Airport>()
            while (cursor.moveToNext()) {
                list.add(
                    Airport(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        iataCode = cursor.getString(cursor.getColumnIndexOrThrow("iata_code")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        passengers = cursor.getInt(cursor.getColumnIndexOrThrow("passengers"))
                    )
                )
            }
            list
        }
    }

    fun getDestinations(departureCode: String): List<Airport> {
        val db = dbHelper.readableDatabase
        return db.rawQuery(
            "SELECT * FROM airport WHERE iata_code != ?",
            arrayOf(departureCode)
        ).use { cursor ->
            val list = mutableListOf<Airport>()
            while (cursor.moveToNext()) {
                list.add(
                    Airport(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        iataCode = cursor.getString(cursor.getColumnIndexOrThrow("iata_code")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        passengers = cursor.getInt(cursor.getColumnIndexOrThrow("passengers"))
                    )
                )
            }
            list
        }
    }

    fun getFavoritesWithNames(): List<FavoriteWithNames> {
        val db = dbHelper.readableDatabase
        return db.rawQuery(
            """SELECT f.id, f.departure_code, f.destination_code,
               d.name AS departureName, dest.name AS destinationName
            FROM favorite f
            INNER JOIN airport d ON f.departure_code = d.iata_code
            INNER JOIN airport dest ON f.destination_code = dest.iata_code""",
            null
        ).use { cursor ->
            val list = mutableListOf<FavoriteWithNames>()
            while (cursor.moveToNext()) {
                list.add(
                    FavoriteWithNames(
                        id = cursor.getInt(0),
                        departureCode = cursor.getString(1),
                        destinationCode = cursor.getString(2),
                        departureName = cursor.getString(3),
                        destinationName = cursor.getString(4)
                    )
                )
            }
            list
        }
    }

    fun isFavorite(dep: String, dest: String): Boolean {
        val db = dbHelper.readableDatabase
        return db.rawQuery(
            "SELECT EXISTS(SELECT 1 FROM favorite WHERE departure_code = ? AND destination_code = ?)",
            arrayOf(dep, dest)
        ).use { cursor ->
            cursor.moveToFirst() && cursor.getInt(0) == 1
        }
    }

    fun addFavorite(dep: String, dest: String) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "INSERT OR IGNORE INTO favorite (departure_code, destination_code) VALUES (?, ?)",
            arrayOf(dep, dest)
        )
    }

    fun removeFavorite(dep: String, dest: String) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "DELETE FROM favorite WHERE departure_code = ? AND destination_code = ?",
            arrayOf(dep, dest)
        )
    }
}