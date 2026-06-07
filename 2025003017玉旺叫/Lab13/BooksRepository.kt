package com.example.bookshelf.data
import com.example.bookshelf.model.Book
import com.example.bookshelf.model.asExternalModel
import com.example.bookshelf.network.apiService

interface BooksRepository {
    suspend fun getBooks(): List<Book>
    suspend fun getBook(id: String): Book
}

class NetworkBooksRepository : BooksRepository {
    override suspend fun getBooks(): List<Book> {
        return apiService.getBooks().map { it.asExternalModel() }
    }
    override suspend fun getBook(id: String): Book {
        return getBooks().first { it.id == id }
    }
}

class OfflineBooksRepository : BooksRepository {
    private val offlineData = listOf(Book("1", "https://picsum.photos/id/10/800/600"))
    override suspend fun getBooks(): List<Book> = offlineData
    override suspend fun getBook(id: String): Book = offlineData.first()
}

class AppContainer {
    val booksRepository: BooksRepository = try {
        NetworkBooksRepository()
    } catch (e: Exception) {
        OfflineBooksRepository()
    }
}