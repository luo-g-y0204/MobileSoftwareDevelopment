package com.example.bookshelf.data

import com.example.bookshelf.network.BookshelfApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.bookshelf.network.BASE_URL

class AppContainer {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: BookshelfApiService = retrofit.create(BookshelfApiService::class.java)

    val networkBooksRepository: BooksRepository = NetworkBooksRepository(apiService)
    val offlineBooksRepository: BooksRepository = OfflineBooksRepository()
    val defaultBooksRepository: BooksRepository = networkBooksRepository
}