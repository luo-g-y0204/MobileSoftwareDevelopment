package com.example.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI状态密封类
sealed interface BookshelfUiState {
    data object Loading : BookshelfUiState
    data class Success(val books: List<Book>) : BookshelfUiState
    data class Error(val message: String) : BookshelfUiState
}

class BookshelfViewModel(
    private val networkRepository: BooksRepository,
    private val offlineRepository: BooksRepository
) : ViewModel() {
    // 核心UI状态
    private val _uiState = MutableStateFlow<BookshelfUiState>(BookshelfUiState.Loading)
    val uiState: StateFlow<BookshelfUiState> = _uiState.asStateFlow()

    // 详情弹窗状态
    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    init {
        loadBooks()
    }

    // 加载书籍数据（自动降级到离线数据）
    fun loadBooks() {
        _uiState.value = BookshelfUiState.Loading
        viewModelScope.launch {
            try {
                val books = networkRepository.getBooks()
                _uiState.value = BookshelfUiState.Success(books)
            } catch (e: Exception) {
                // 网络失败自动使用离线数据
                try {
                    val offlineBooks = offlineRepository.getBooks()
                    _uiState.value = BookshelfUiState.Success(offlineBooks)
                } catch (ex: Exception) {
                    _uiState.value = BookshelfUiState.Error("加载失败：${e.message ?: "未知错误"}")
                }
            }
        }
    }

    // 手动重试
    fun retry() {
        loadBooks()
    }

    // 选择书籍并显示详情
    fun selectBook(book: Book) {
        _selectedBook.value = book
        _showDialog.value = true
    }

    // 关闭详情弹窗
    fun dismissDialog() {
        _showDialog.value = false
        _selectedBook.value = null
    }
}

// ViewModel工厂类（解决构造函数传参问题）
class BookshelfViewModelFactory(
    private val networkRepository: BooksRepository,
    private val offlineRepository: BooksRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookshelfViewModel::class.java)) {
            return BookshelfViewModel(networkRepository, offlineRepository) as T
        }
        throw IllegalArgumentException("未知的ViewModel类型")
    }
}