package com.example.bookshelf.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookshelf.data.AppContainer
import com.example.bookshelf.model.Book
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface BookshelfUiState {
    data object Loading : BookshelfUiState
    data class Success(val books: List<Book>) : BookshelfUiState
    data class Error(val msg: String) : BookshelfUiState
}

data class DetailUiState(
    val showDialog: Boolean = false,
    val selectBook: Book? = null
)

class BookshelfViewModel(container: AppContainer) : ViewModel() {
    companion object Factory
    private val repo = container.booksRepository

    private val _uiState = MutableStateFlow<BookshelfUiState>(BookshelfUiState.Loading)
    val uiState: StateFlow<BookshelfUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(DetailUiState())
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    init {
        reloadData()
    }

    fun reloadData() {
        _uiState.value = BookshelfUiState.Loading
        viewModelScope.launch {
            try {
                val list = repo.getBooks()
                _uiState.value = BookshelfUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = BookshelfUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun openDetail(book: Book) = _detailState.update { it.copy(showDialog = true, selectBook = book) }
    fun closeDetail() = _detailState.update { it.copy(showDialog = false, selectBook = null) }
}