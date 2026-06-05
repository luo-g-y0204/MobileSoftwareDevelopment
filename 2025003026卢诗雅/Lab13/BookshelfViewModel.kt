package com.example.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookshelfViewModel(
    private val repo: BooksRepository
) : ViewModel() {
    private val _ui = MutableStateFlow<BookshelfUiState>(BookshelfUiState.Loading)
    val ui: StateFlow<BookshelfUiState> = _ui.asStateFlow()

    private val _detail = MutableStateFlow(BookDetailUiState())
    val detail: StateFlow<BookDetailUiState> = _detail.asStateFlow()

    init {
        load()
    }

    fun load() {
        _ui.value = BookshelfUiState.Loading
        viewModelScope.launch {
            try {
                val list = repo.getBooks()
                _ui.value = BookshelfUiState.Success(list)
            } catch (e: Exception) {
                _ui.value = BookshelfUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun showDetail(book: Book) {
        _detail.update { it.copy(show = true, book = book) }
    }

    fun hideDetail() {
        _detail.update { it.copy(show = false, book = null) }
    }
}

class BookshelfViewModelFactory(
    private val repo: BooksRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(cls: Class<T>): T {
        return BookshelfViewModel(repo) as T
    }
}