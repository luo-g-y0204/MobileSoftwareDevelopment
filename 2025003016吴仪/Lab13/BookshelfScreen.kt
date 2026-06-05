package com.example.bookshelf.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.bookshelf.model.Book

@Composable
fun BookshelfScreen(vm: BookshelfViewModel) {
    val uiState = vm.uiState.collectAsStateWithLifecycle()
    // 弹窗状态：记录当前选中的图书
    var selectBook: Book? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState.value) {
            is BookshelfUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is BookshelfUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.list) { book ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                // 修复点击：赋值选中图书，唤起弹窗
                                .clickable { selectBook = book }
                        ) {
                            Column {
                                AsyncImage(
                                    model = book.imgUrl,
                                    contentDescription = book.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .size(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = book.title,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }
            is BookshelfUiState.Error -> {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = state.msg)
                }
            }
        }

        // 弹窗：选中图书不为空时弹出详情
        selectBook?.let { book ->
            AlertDialog(
                onDismissRequest = { selectBook = null },
                title = { Text(book.title) },
                text = {
                    AsyncImage(
                        model = book.imgUrl,
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxWidth().size(320.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                confirmButton = {
                    Text(text = "关闭", modifier = Modifier.clickable { selectBook = null })
                }
            )
        }
    }
}