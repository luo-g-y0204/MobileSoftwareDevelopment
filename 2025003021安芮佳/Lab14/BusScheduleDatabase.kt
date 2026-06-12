package com.example.busschedule.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BusSchedule::class], // 声明该数据库包含的实体
    version = 1,                     // 数据库版本号
    exportSchema = false
)
abstract class BusScheduleDatabase : RoomDatabase() {
    // 提供一个抽象方法，用于获取 DAO 实例
    abstract fun busScheduleDao(): BusScheduleDao

    companion object {
        // Volatile 保证 instance 的可见性，避免多个线程同时访问时出现问题
        @Volatile
        private var INSTANCE: BusScheduleDatabase? = null

        fun getDatabase(context: Context): BusScheduleDatabase {
            // 单例模式：如果实例已存在则直接返回，否则创建新实例
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,      // 使用 applicationContext 防止内存泄漏
                    BusScheduleDatabase::class.java, // 数据库类的 KClass
                    "bus_schedule_database"          // 数据库文件名
                )
                    // 关键步骤：从 assets 目录下的预置数据库文件创建并初始化数据库
                    .createFromAsset("database/bus_schedule.db")
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}