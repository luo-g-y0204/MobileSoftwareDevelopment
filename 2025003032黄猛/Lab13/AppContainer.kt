package com.example.bookshelf

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(
    val booksRepository: BooksRepository
)

fun createDefaultAppContainer(): AppContainer {
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(BookshelfApiService::class.java)
    val networkRepo = NetworkBooksRepository(api)

    return AppContainer(booksRepository = networkRepo)
}
