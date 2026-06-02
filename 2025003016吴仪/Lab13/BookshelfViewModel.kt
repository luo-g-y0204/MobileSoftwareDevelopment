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

sealed interface BookshelfUiState {
    object Loading: BookshelfUiState
    data class Success(val list:List<Book>):BookshelfUiState
    data class Error(val msg:String):BookshelfUiState
}

// 构造函数接收外部传入Repository
class BookshelfViewModel(private val repo: BooksRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BookshelfUiState>(BookshelfUiState.Loading)
    val uiState:StateFlow<BookshelfUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { BookshelfUiState.Loading }
        viewModelScope.launch {
            try {
                val data = repo.getBooks()
                _uiState.update { BookshelfUiState.Success(data) }
            }catch (ex:Exception){
                _uiState.update { BookshelfUiState.Error(ex.message ?: "加载失败") }
            }
        }
    }
}