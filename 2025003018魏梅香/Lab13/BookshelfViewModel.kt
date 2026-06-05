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

/**
 * UI状态密封接口（覆盖加载中/成功/失败）
 */
sealed interface BookshelfUiState {
    data object Loading : BookshelfUiState
    data class Success(val books: List<Book>) : BookshelfUiState
    data class Error(val message: String) : BookshelfUiState
}

/**
 * 书架ViewModel（处理业务逻辑和状态管理）
 */
class BookshelfViewModel(
    private val booksRepository: BooksRepository // 补全导入
) : ViewModel() {

    // 私有可变UI状态
    private val _uiState = MutableStateFlow<BookshelfUiState>(BookshelfUiState.Loading)
    // 公开不可变UI状态
    val uiState: StateFlow<BookshelfUiState> = _uiState.asStateFlow()

    // 选中的书籍（用于详情弹窗）
    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    init {
        loadBooks()
    }

    /**
     * 加载书籍数据（支持重试）
     */
    fun loadBooks() {
        _uiState.value = BookshelfUiState.Loading
        viewModelScope.launch {
            try {
                val books = booksRepository.getBooks()
                _uiState.value = BookshelfUiState.Success(books)
            } catch (e: Exception) {
                _uiState.value = BookshelfUiState.Error(e.message ?: "加载失败，请重试")
            }
        }
    }

    /**
     * 选择书籍（打开详情弹窗）
     */
    fun selectBook(book: Book) {
        _selectedBook.update { book }
    }

    /**
     * 关闭详情弹窗
     */
    fun dismissBookDetail() {
        _selectedBook.update { null }
    }
}