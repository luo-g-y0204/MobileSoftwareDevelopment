package com.example.bookshelf.data

interface AppContainer {
    val booksRepository: BooksRepository
}

class DefaultAppContainer : AppContainer {
    override val booksRepository: BooksRepository = try {
        NetworkBooksRepository()
    } catch (e: Exception) {
        OfflineBooksRepository()
    }
}