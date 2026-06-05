package com.example.bookshelf

import retrofit2.http.GET

interface BookshelfApiService {
    @GET("photos")
    suspend fun getBooks(): List<BookDto>
}
