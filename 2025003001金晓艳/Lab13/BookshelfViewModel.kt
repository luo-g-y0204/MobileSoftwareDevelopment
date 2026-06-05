package com.example.bookshelf.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookshelf.BookshelfApplication
import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.model.Book
import kotlinx.coroutines.launch

sealed interface BookshelfUiState {
    object Loading : BookshelfUiState
    data class Success(val books: List<Book>) : BookshelfUiState
    object Error : BookshelfUiState
}

class BookshelfViewModel(
    private val repository: BooksRepository
) : ViewModel() {

    var uiState: BookshelfUiState by mutableStateOf(BookshelfUiState.Loading)
        private set

    var selectedBook: Book? by mutableStateOf(null)
    val showDetail get() = selectedBook != null

    init {
        loadBooks()
    }

    fun loadBooks() {
        uiState = BookshelfUiState.Loading
        viewModelScope.launch {
            uiState = try {
                BookshelfUiState.Success(repository.getBooks())
            } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
                BookshelfUiState.Error
            }
        }
    }

    fun openDetail(book: Book) {
        selectedBook = book
    }

    fun closeDetail() {
        selectedBook = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                // 修复点：改用 CreationExtras 获取Application
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookshelfApplication
                BookshelfViewModel(app.container.booksRepository)
            }
        }
    }
}