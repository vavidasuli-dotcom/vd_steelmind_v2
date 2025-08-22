package com.vd.steelmind.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vd.steelmind.data.AppDatabase
import com.vd.steelmind.data.ItemTypeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCatalogScreen(nav: NavController, db: AppDatabase) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("0") }
    var items by remember { mutableStateOf(listOf<ItemTypeEntity>()) }

    fun refresh() { scope.launch(Dispatchers.IO) { items = db.appDao().listItemTypes() } }
    LaunchedEffect(Unit) { refresh() }

    Scaffold(topBar = { TopAppBar(title = { Text("Tételkezelő (Ft/m)") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Tétel neve") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(material, { material = it }, label = { Text("Anyagtípus (opcionális)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(price, { price = it }, label = { Text("Ár (Ft/m)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    db.appDao().insertItemType(com.vd.steelmind.data.ItemTypeEntity(name = name, materialType = material.ifBlank { null }, priceFtPerM = price.toIntOrNull() ?: 0))
                    name = ""; material = ""; price = "0"; refresh()
                }
            }) { Text("Hozzáadás") }
            Divider(Modifier.padding(vertical = 12.dp))
            LazyColumn {
                items(items) { itype ->
                    ListItem(
                        headlineContent = { Text("${itype.name} – ${itype.priceFtPerM} Ft/m") },
                        supportingContent = { Text(itype.materialType ?: "Anyag: -") }
                    )
                    Divider()
                }
            }
        }
    }
}
