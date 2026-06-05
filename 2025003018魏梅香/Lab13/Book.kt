package com.example.bookshelf.model

data class Book(
    val id: String,
    val coverUrl: String,
    val title: String = "图书 $id"
)

fun BookDto.asExternalModel(): Book {
    return Book(
        id = this.id,
        coverUrl = this.imgSrc
    )
}

fun List<BookDto>.toBookList(): List<Book> {
    return map { it.asExternalModel() }
}