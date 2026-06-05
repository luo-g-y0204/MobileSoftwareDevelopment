package com.example.bookshelf.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookshelf.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(vm: BookshelfViewModel) {
    val ui = vm.ui.collectAsState()
    val detail = vm.detail.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Bookshelf") })
        }
    ) { inner ->
        Box(modifier = Modifier.fillMaxSize().padding(inner)) {
            when (val s = ui.value) {
                is BookshelfUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is BookshelfUiState.Success ->
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(s.books) { book ->
                            Card(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { vm.showDetail(book) }
                            ) {
                                AsyncImage(
                                    model = book.coverUrl,
                                    contentDescription = book.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = book.title,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                is BookshelfUiState.Error ->
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(s.msg, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { vm.load() }) { Text("Retry") }
                    }
            }
        }
    }

    if (detail.value.show && detail.value.book != null) {
        AlertDialog(
            onDismissRequest = { vm.hideDetail() },
            title = { Text(detail.value.book!!.title) },
            text = {
                AsyncImage(
                    model = detail.value.book!!.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            },
            confirmButton = {
                IconButton(onClick = { vm.hideDetail() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        )
    }
}