package com.example.bookshelf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bookshelf.ui.BookshelfUiState
import com.example.bookshelf.ui.BookshelfViewModel

@Composable
fun BookshelfScreen(
    viewModel: BookshelfViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is BookshelfUiState.Loading -> Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
            is BookshelfUiState.Error -> Text(text = (uiState as BookshelfUiState.Error).msg)
            is BookshelfUiState.Success -> {
                val bookList = (uiState as BookshelfUiState.Success).books
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.padding(all = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bookList) { book ->
                        AsyncImage(
                            model = book.coverUrl,
                            contentDescription = book.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openDetail(book) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                if (detailState.showDialog && detailState.selectBook != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.closeDetail() },
                        title = { Text(detailState.selectBook!!.title) },
                        text = { AsyncImage(model = detailState.selectBook!!.coverUrl, contentDescription = null) },
                        confirmButton = {
                            Button(onClick = { viewModel.closeDetail() }) {
                                Text("关闭")
                            }
                        }
                    )
                }
            }
        }
    }
}