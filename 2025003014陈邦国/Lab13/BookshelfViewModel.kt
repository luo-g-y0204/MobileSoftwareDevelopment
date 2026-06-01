package com.example.myapplicationlab10.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplicationlab10.bookshelf.BookshelfApplication
import com.example.myapplicationlab10.bookshelf.data.BooksRepository
import com.example.myapplicationlab10.bookshelf.data.OfflineBooksRepository
import com.example.myapplicationlab10.bookshelf.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel管理UI状态
 * 三种状态：加载、成功、错误
 * 处理加载、重试、详情逻辑
 * 使用viewModelScope管理协程
 */
sealed interface BookshelfUiState {
    object Loading : BookshelfUiState
    data class Success(val books: List<Book>) : BookshelfUiState
    data class Error(val message: String) : BookshelfUiState
}

class BookshelfViewModel(
    private val booksRepository: BooksRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<BookshelfUiState>(BookshelfUiState.Loading)
    val uiState: StateFlow<BookshelfUiState> = _uiState.asStateFlow()

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        _uiState.value = BookshelfUiState.Loading
        viewModelScope.launch {
            try {
                val list = booksRepository.getBooks()
                _uiState.value = BookshelfUiState.Success(list)
            } catch (e: Exception) {
                val offline = OfflineBooksRepository().getBooks()
                _uiState.value = BookshelfUiState.Success(offline)
            }
        }
    }

    fun selectBook(book: Book) {
        _selectedBook.value = book
    }

    fun closeDetailDialog() {
        _selectedBook.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as BookshelfApplication)
                BookshelfViewModel(app.container.booksRepository)
            }
        }
    }
}