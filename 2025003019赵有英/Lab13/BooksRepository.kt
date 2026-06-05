package com.example.bookshelf.data

import com.example.bookshelf.model.Book
import com.example.bookshelf.model.asExternalModel
import com.example.bookshelf.network.BookshelfApiService

// 仓库接口，定义数据操作规范
interface BooksRepository {
    suspend fun getBooks(): List<Book>
    suspend fun getBook(id: String): Book?
}

// 网络数据源实现
class NetworkBooksRepository(
    private val apiService: BookshelfApiService
) : BooksRepository {
    override suspend fun getBooks(): List<Book> {
        return apiService.getBooks().asExternalModelList()
    }

    override suspend fun getBook(id: String): Book? {
        return getBooks().find { it.id == id }
    }
}

// 离线兜底数据源实现（断网时使用）
class OfflineBooksRepository : BooksRepository {
    private val offlineBooks = listOf(
        Book("1", "https://picsum.photos/id/10/800/600"),
        Book("2", "https://picsum.photos/id/11/800/600"),
        Book("3", "https://picsum.photos/id/12/800/600"),
        Book("4", "https://picsum.photos/id/13/800/600"),
        Book("5", "https://picsum.photos/id/14/800/600"),
        Book("6", "https://picsum.photos/id/15/800/600"),
        Book("7", "https://picsum.photos/id/16/800/600"),
        Book("8", "https://picsum.photos/id/17/800/600"),
        Book("9", "https://picsum.photos/id/18/800/600"),
        Book("10", "https://picsum.photos/id/19/800/600"),
        Book("11", "https://picsum.photos/id/20/800/600"),
        Book("12", "https://picsum.photos/id/21/800/600"),
        Book("13", "https://picsum.photos/id/22/800/600"),
        Book("14", "https://picsum.photos/id/23/800/600"),
        Book("15", "https://picsum.photos/id/24/800/600"),
        Book("16", "https://picsum.photos/id/25/800/600"),
        Book("17", "https://picsum.photos/id/26/800/600"),
        Book("18", "https://picsum.photos/id/27/800/600"),
        Book("19", "https://picsum.photos/id/28/800/600"),
        Book("20", "https://picsum.photos/id/29/800/600")
    )

    override suspend fun getBooks(): List<Book> {
        return offlineBooks
    }

    override suspend fun getBook(id: String): Book? {
        return offlineBooks.find { it.id == id }
    }
}