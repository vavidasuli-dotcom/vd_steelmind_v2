package com.vd.steelmind.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vd.steelmind.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(nav: NavController, db: AppDatabase) {
    val scope = rememberCoroutineScope()
    var length by remember { mutableStateOf("6000") }
    var count by remember { mutableStateOf("10") }
    var stock by remember { mutableStateOf(listOf<InventoryBarEntity>()) }
    var offcuts by remember { mutableStateOf(listOf<OffcutEntity>()) }

    fun refresh() {
        scope.launch(Dispatchers.IO) {
            stock = db.appDao().listInventory()
            offcuts = db.appDao().listOffcuts()
        }
    }
    LaunchedEffect(Unit) { refresh() }

    Scaffold(topBar = { TopAppBar(title = { Text("Raktárkészlet & Maradékok") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Row {
                OutlinedTextField(length, { length = it }, label = { Text("Szálhossz (mm)") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(count, { count = it }, label = { Text("Darabszám") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    db.appDao().upsertInventoryBar(InventoryBarEntity(lengthMm = length.toIntOrNull() ?: 0, count = count.toIntOrNull() ?: 0))
                    refresh()
                }
            }) { Text("Készlet mentése") }

            Divider(Modifier.padding(vertical = 12.dp))
            Text("Készlet:")
            LazyColumn {
                items(stock) { s ->
                    ListItem(headlineContent = { Text("${s.lengthMm} mm × ${s.count}") })
                    Divider()
                }
            }
            Divider(Modifier.padding(vertical = 12.dp))
            Text("Maradékok:")
            LazyColumn {
                items(offcuts) { o ->
                    ListItem(headlineContent = { Text("${o.lengthMm} mm × ${o.count}") })
                    Divider()
                }
            }
        }
    }
}
