package com.example.myapplicationlab10.bookshelf

import com.example.myapplicationlab10.bookshelf.data.BooksRepository
import com.example.myapplicationlab10.bookshelf.data.NetworkBooksRepository
import com.example.myapplicationlab10.bookshelf.data.OfflineBooksRepository
import com.example.myapplicationlab10.bookshelf.network.BookshelfApiService
import com.example.myapplicationlab10.bookshelf.network.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 全局依赖容器
 * 统一创建Retrofit、ApiService、Repository实例
 * 简单依赖注入，便于管理
 * 优先网络仓库，失败切离线
 */
interface AppContainer {
    val booksRepository: BooksRepository
}

class DefaultAppContainer : AppContainer {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: BookshelfApiService by lazy {
        retrofit.create(BookshelfApiService::class.java)
    }

    override val booksRepository: BooksRepository by lazy {
        try {
            NetworkBooksRepository(apiService)
        } catch (e: Exception) {
            OfflineBooksRepository()
        }
    }
}