package com.example.bookshelf

import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.data.NetworkBooksRepository
import com.example.bookshelf.data.OfflineBooksRepository
import com.example.bookshelf.network.ApiConfig
import com.example.bookshelf.network.BookshelfApiService
import com.example.bookshelf.ui.BookshelfViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer {
    // Retrofit实例初始化（仅修改BASE_URL引用）
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(ApiConfig.BASE_URL)
        .build()

    // API服务实例
    private val bookshelfApiService: BookshelfApiService by lazy {
        retrofit.create(BookshelfApiService::class.java)
    }

    // 仓库实例
    val networkRepository: BooksRepository by lazy {
        NetworkBooksRepository(bookshelfApiService)
    }

    val offlineRepository: BooksRepository by lazy {
        OfflineBooksRepository()
    }

    // ViewModel工厂（用于传递依赖）
    val bookshelfViewModelFactory: BookshelfViewModelFactory by lazy {
        BookshelfViewModelFactory(networkRepository, offlineRepository)
    }
}