package com.example.bookshelf.data.network

import com.example.bookshelf.model.BookDto
import retrofit2.http.GET

interface BookshelfApiService {
    @GET("photos")
    suspend fun getBookList(): List<BookDto>
}