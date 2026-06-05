package com.example.myapplicationlab10.bookshelf

import android.app.Application

/**
 * 自定义Application类
 * 应用全局入口
 * 初始化依赖容器
 * 提供全局Repository访问
 */
class BookshelfApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}