package com.example.flightsearch

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room 数据库类
 * 使用 createFromAsset() 从预置的 flight_search.db 文件初始化数据库
 * 采用单例模式确保全局只有一个数据库实例
 */
@Database(
    entities = [Airport::class, Favorite::class],
    version = 1,
    exportSchema = false
)
abstract class FlightDatabase : RoomDatabase() {

    /**
     * 提供 DAO 实例
     */
    abstract fun flightDao(): FlightDao

    companion object {
        @Volatile
        private var INSTANCE: FlightDatabase? = null

        /**
         * 获取数据库单例
         *
         * @param context 应用上下文
         * @return FlightDatabase 实例
         */
        fun getDatabase(context: Context): FlightDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlightDatabase::class.java,
                    "flight_search.db"
                )
                    // 从 assets/database/ 目录加载预置数据库
                    .createFromAsset("database/flight_search.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
