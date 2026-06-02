package com.example.bookshelf

import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.data.NetworkBooksRepository
import com.example.bookshelf.data.OfflineBooksRepository
import com.example.bookshelf.data.network.RetrofitInstance

class AppContainer {
    val booksRepository: BooksRepository = try {
        NetworkBooksRepository(RetrofitInstance.api)
    } catch (e: Exception) {
        OfflineBooksRepository()
    }
}