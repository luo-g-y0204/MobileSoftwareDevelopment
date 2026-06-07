package com.example.bookshelf.model

data class Book(
    val id: String,
    val coverUrl: String,
    val title: String = "Book $id"
)

fun BookDto.asExternalModel(): Book {
    return Book(id, imgSrc)
}