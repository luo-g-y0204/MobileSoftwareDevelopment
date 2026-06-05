package com.example.bookshelf

import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.data.NetworkBooksRepository
import com.example.bookshelf.data.OfflineBooksRepository
import com.example.bookshelf.network.provideBookshelfApiService
import com.example.bookshelf.network.provideRetrofit

interface AppContainer {
    val booksRepository: BooksRepository
}

class DefaultAppContainer : AppContainer {
    private val retrofit = provideRetrofit()
    private val apiService = provideBookshelfApiService(retrofit)

    override val booksRepository: BooksRepository by lazy {
        try {
            NetworkBooksRepository(apiService)
        } catch (e: Exception) {
            OfflineBooksRepository()
        }
    }
}