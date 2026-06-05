package com.example.bookshelf.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 书架API服务接口
 * 实验要求使用Retrofit定义网络请求
 * 所有方法均为suspend函数，支持协程调用
 * @author 你的姓名
 * @date 2026-06-03
 */
interface BookshelfApiService {
    /** 实验要求的获取所有书籍列表接口 */
    @GET("photos")
    suspend fun getBooks(): List<BookDto>

    /** 根据ID获取单本书籍（配合实验要求的详情功能） */
    @GET("photos/{id}")
    suspend fun getBookById(@Path("id") id: String): BookDto
}