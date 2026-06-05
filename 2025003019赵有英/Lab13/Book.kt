package com.example.bookshelf.model

import com.example.bookshelf.network.BookDto

data class Book(
    val id: String,
    val coverUrl: String,
    val title: String = "Book #$id"
)

fun BookDto.asExternalModel(): Book {
    return Book(
        id = this.id,
        coverUrl = this.imgSrc
    )
}