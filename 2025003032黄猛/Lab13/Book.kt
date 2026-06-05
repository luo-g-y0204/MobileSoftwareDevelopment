package com.example.bookshelf

data class Book(
    val id: String,
    val coverUrl: String,
    val title: String
)

fun BookDto.asExternalModel(): Book = Book(
    id = id,
    coverUrl = imgSrc,
    title = "Book #$id"
)
