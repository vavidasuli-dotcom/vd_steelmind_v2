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

data class DemandRow(val id: Long, val itemName: String, val length: Int, val qty: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemandScreen(nav: NavController, db: AppDatabase, projectId: Long) {
    val scope = rememberCoroutineScope()
    var project by remember { mutableStateOf<ProjectEntity?>(null) }
    var items by remember { mutableStateOf(listOf<ItemTypeEntity>()) }
    var demands by remember { mutableStateOf(listOf<DemandRow>()) }

    var length by remember { mutableStateOf("1000") }
    var qty by remember { mutableStateOf("1") }
    var selectedItemId by remember { mutableStateOf<Long?>(null) }

    fun refresh() {
        scope.launch(Dispatchers.IO) {
            project = db.appDao().listProjects().find { it.id == projectId }
            items = db.appDao().listItemTypes()
            val ds = db.appDao().listDemands(projectId)
            val itemsById = items.associateBy { it.id }
            demands = ds.map {
                DemandRow(it.id, itemsById[it.itemTypeId]?.name ?: "Ismeretlen", it.lengthMm, it.quantity)
            }
        }
    }
    LaunchedEffect(Unit) { refresh() }

    Scaffold(topBar = { TopAppBar(title = { Text("Darabolandó méretek") }) },
        floatingActionButton = { FloatingActionButton(onClick = { nav.navigate("plan/$projectId") }) { Text("Terv") } }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Projekt: ${project?.name ?: ""}")
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = items.find { it.id == selectedItemId }?.name ?: "Tétel választása",
                    onValueChange = {}, readOnly = true, label = { Text("Tétel") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    items.forEach { item ->
                        DropdownMenuItem(text = { Text(item.name) }, onClick = { selectedItemId = item.id; expanded = false })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(length, { length = it }, label = { Text("Hossz (mm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(qty, { qty = it }, label = { Text("Darabszám") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(enabled = selectedItemId != null, onClick = {
                scope.launch(Dispatchers.IO) {
                    db.appDao().insertCutDemand(
                        CutDemandEntity(
                            projectId = projectId,
                            itemTypeId = selectedItemId!!,
                            lengthMm = length.toIntOrNull() ?: 0,
                            quantity = qty.toIntOrNull() ?: 1
                        )
                    )
                    refresh()
                }
            }) { Text("Hozzáadás") }

            Divider(Modifier.padding(vertical = 12.dp))
            LazyColumn {
                items(demands) { d ->
                    ListItem(headlineContent = { Text("${d.itemName}: ${d.length} mm × ${d.qty}") })
                    Divider()
                }
            }
        }
    }
}
