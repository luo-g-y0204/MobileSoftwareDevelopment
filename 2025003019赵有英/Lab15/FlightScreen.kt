package com.example.flightsearch.ui

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.FavoriteFlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightScreen(modifier: Modifier = Modifier) {
    val vm: FlightViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val appKey = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                FlightViewModel(this[appKey] as Application)
            }
        }
    )

    val searchTxt = vm.searchText
    val selectedDep = vm.selectedDepartAirport
    val suggestList by vm.airportSuggestions.collectAsState()
    val destList by vm.destinationFlights.collectAsState()
    val favList by vm.allFavoriteFlights.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("航班搜索") }) }) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SearchInputField(text = searchTxt, onTextChange = vm::updateSearchText)
            Spacer(modifier = Modifier.height(16.dp))

            when {
                // 已选中出发机场 → 展示目的地航班
                selectedDep != null -> {
                    FlightDestList(
                        departAirport = selectedDep,
                        destinations = destList,
                        checkFav = { d1, d2 -> vm.isRouteFavorite(d1, d2) },
                        toggleFav = { d1, d2 ->
                            if(vm.isRouteFavorite(d1,d2)) vm.removeFavorite(d1,d2)
                            else vm.addFavorite(d1,d2)
                        }
                    )
                }
                // 有输入文字 → 展示搜索建议
                searchTxt.isNotBlank() -> {
                    AirportSuggestList(list = suggestList, onClick = vm::selectDepartAirport)
                }
                // 无输入 → 展示收藏列表
                else -> {
                    FavoriteFlightList(list = favList, onDel = vm::removeFavorite)
                }
            }
        }
    }
}

// 搜索输入框
@Composable
fun SearchInputField(text: String, onTextChange: (String)->Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("输入机场名称 / IATA三字码") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索图标") }
    )
}

// 搜索机场建议列表
@Composable
fun AirportSuggestList(list: List<Airport>, onClick: (Airport)->Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(list, key = {it.id}) { item ->
            Card(onClick = onClick(item), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.iataCode, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.width(60.dp))
                    Text(item.name, fontSize = 16.sp)
                }
            }
        }
    }
}

// 选中机场后的目的地航班页面
@Composable
fun FlightDestList(
    departAirport: Airport,
    destinations: List<Airport>,
    checkFav: (String,String)->Boolean,
    toggleFav: (String,String)->Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("出发：${departAirport.iataCode} ${departAirport.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(destinations, key = {it.id}) { dest ->
                FlightItemCard(
                    depCode = departAirport.iataCode,
                    depName = departAirport.name,
                    destCode = dest.iataCode,
                    destName = dest.name,
                    isFav = checkFav(departAirport.iataCode, dest.iataCode),
                    onToggle = { toggleFav(departAirport.iataCode, dest.iataCode) }
                )
            }
        }
    }
}

// 收藏列表页面
@Composable
fun FavoriteFlightList(list: List<FavoriteFlight>, onDel: (String,String)->Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("我的收藏航班", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        if(list.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无收藏，搜索机场添加航线", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list, key = {it.id}) { fav ->
                    FlightItemCard(
                        depCode = fav.departureCode,
                        depName = fav.departureName,
                        destCode = fav.destinationCode,
                        destName = fav.destinationName,
                        isFav = true,
                        onToggle = { onDel(fav.departureCode, fav.destinationCode) }
                    )
                }
            }
        }
    }
}

// 通用航班卡片组件
@Composable
fun FlightItemCard(
    depCode: String,
    depName: String,
    destCode: String,
    destName: String,
    isFav: Boolean,
    onToggle: ()->Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(depCode, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("→", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(destCode, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("$depName → $destName", fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if(isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    tint = if(isFav) Color.Red else Color.Gray,
                    contentDescription = if(isFav) "取消收藏" else "添加收藏"
                )
            }
        }
    }
}