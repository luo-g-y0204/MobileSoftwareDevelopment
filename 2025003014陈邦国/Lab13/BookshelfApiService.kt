package com.example.myapplicationlab10.bookshelf.network

import retrofit2.http.GET
import com.example.myapplicationlab10.bookshelf.model.BookDto

/**
 * Retrofit网络服务接口
 * 定义与Mock服务器交互的请求方法
 * 使用GET请求获取书籍列表
 * 挂起函数配合协程实现异步
 * 返回值为BookDto列表，对应JSON数组
 */
interface BookshelfApiService {
    @GET("photos")
    suspend fun getBooks(): List<BookDto>
}