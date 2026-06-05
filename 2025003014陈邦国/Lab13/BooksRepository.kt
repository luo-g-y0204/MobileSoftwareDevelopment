package com.example.myapplicationlab10.bookshelf.data

import com.example.myapplicationlab10.bookshelf.model.Book

/**
 * 书籍仓库接口
 * 定义数据层统一访问规范
 * 解耦ViewModel与数据源
 * 实现网络、离线两种数据源
 * 包含获取列表、获取单本书方法
 */
interface BooksRepository {
    suspend fun getBooks(): List<Book>
    suspend fun getBook(id: String): Book
}