package com.example.bookshelf.network
import com.example.bookshelf.model.BookDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface BookshelfApiService {
    @GET("photos")
    suspend fun getBooks(): List<BookDto>
}

val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService: BookshelfApiService = retrofit.create(BookshelfApiService::class.java)