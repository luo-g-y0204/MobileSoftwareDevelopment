package com.example.bookshelf.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://m1.apifoxmock.com/m1/8321477-8085280-default/"

fun provideRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun provideBookshelfApiService(retrofit: Retrofit): BookshelfApiService {
    return retrofit.create(BookshelfApiService::class.java)
}