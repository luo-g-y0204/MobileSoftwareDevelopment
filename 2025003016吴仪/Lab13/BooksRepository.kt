package com.example.bookshelf.data

import com.example.bookshelf.model.Book
//图书仓库顶层接口
interface BooksRepository {
    suspend fun getBooks(): List<Book>
}