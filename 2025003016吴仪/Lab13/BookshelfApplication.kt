package com.example.bookshelf

import android.app.Application
import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.data.NetworkBooksRepository
import com.example.bookshelf.network.RetrofitInstance

interface AppContainer {
    val booksRepository: BooksRepository
}

class DefaultAppContainer : AppContainer {
    override val booksRepository: BooksRepository by lazy {
        NetworkBooksRepository(RetrofitInstance.apiService)
    }
}

class OfflineAppContainer : AppContainer {
    override val booksRepository: BooksRepository by lazy {
        com.example.bookshelf.data.OfflineBooksRepository()
    }
}

class BookshelfApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}