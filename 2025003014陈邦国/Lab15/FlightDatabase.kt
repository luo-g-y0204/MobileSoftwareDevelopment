package com.example.flightsearch.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class FlightDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "flight_search.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: FlightDatabase? = null

        fun getInstance(context: Context): FlightDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FlightDatabase(context.applicationContext).also {
                    it.ensureDatabaseCopied(context)
                    INSTANCE = it
                }
            }
        }
    }

    private fun ensureDatabaseCopied(context: Context) {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (dbFile.exists() && dbFile.length() > 0) return

        dbFile.parentFile?.mkdirs()
        try {
            // 修复：路径从 database 改为 databases，与你的assets目录完全一致
            context.assets.open("databases/$DATABASE_NAME").use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("FlightDB", "数据库文件复制成功")
        } catch (e: Exception) {
            Log.e("FlightDB", "数据库文件复制失败，将使用空表结构兜底", e)
            // 复制失败不闪退，依靠onCreate自动建表保证基础运行
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS airport (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                iata_code TEXT NOT NULL UNIQUE,
                name TEXT NOT NULL,
                passengers INTEGER NOT NULL DEFAULT 0
            )
        """)
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS favorite (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                departure_code TEXT NOT NULL,
                destination_code TEXT NOT NULL,
                UNIQUE(departure_code, destination_code)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // 后续版本升级可在此处理表结构变更
    }

    private var dao: FlightDao? = null
    fun flightDao(): FlightDao = dao ?: FlightDao(this).also { dao = it }
}