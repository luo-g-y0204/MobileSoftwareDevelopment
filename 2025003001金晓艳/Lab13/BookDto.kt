package com.example.bookshelf.model

import com.google.gson.annotations.SerializedName

data class BookDto(
    val id: String = "",
    @SerializedName("img_src") val imgSrc: String = ""
)

fun BookDto.toBook(): Book {
    return Book(id = id, coverUrl = imgSrc)
}

fun List<BookDto>.toBookList(): List<Book> {
    return map { it.toBook() }
}