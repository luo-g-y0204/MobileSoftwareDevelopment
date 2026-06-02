package com.example.bookshelf.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://m1.apifoxmock.com/m1/8321477-8085280-default/"

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: BookshelfApiService by lazy {
        retrofit.create(BookshelfApiService::class.java)
    }
}