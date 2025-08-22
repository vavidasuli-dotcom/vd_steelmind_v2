package com.vd.steelmind.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vd.steelmind.data.AppDatabase
import com.vd.steelmind.data.ProjectEntity
import com.vd.steelmind.data.StockOptionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProjectEditorScreen(nav: NavController, db: AppDatabase, projectId: Long?) {
    val ctx = LocalContext.current
    val prefs = ctx.getSharedPreferences("vd_cut_settings", Context.MODE_PRIVATE)
    var name by remember { mutableStateOf("Új projekt") }
    var material by remember { mutableStateOf("") }
    var defaultKerf by remember { mutableStateOf((prefs.getInt("defaultKerf", 3)).toString()) }
    var minOffcut by remember { mutableStateOf((prefs.getInt("minOffcut", 200)).toString()) }
    var useInventory by remember { mutableStateOf(false) }
    var stockLengths by remember { mutableStateOf(prefs.getString("defaultStock", "6000") ?: "6000") }

    Scaffold(topBar = { TopAppBar(title = { Text("Projekt létrehozása") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val pid = db.appDao().insertProject(
                        ProjectEntity(
                            name = name,
                            materialType = material.ifBlank { null },
                            useInventory = useInventory,
                            kerfMmDefault = defaultKerf.toIntOrNull() ?: 3,
                            minOffcutMm = minOffcut.toIntOrNull() ?: 200
                        )
                    )
                    db.appDao().clearStockOptions(pid)
                    stockLengths.split(",").mapNotNull { it.trim().toIntOrNull() }.forEach { len ->
                        db.appDao().insertStockOption(StockOptionEntity(projectId = pid, lengthMm = len, availableCount = null))
                    }
                }
                nav.popBackStack()
            }) { Text("Mentés") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Projekt név") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(material, { material = it }, label = { Text("Anyagtípus (opcionális)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(defaultKerf, { defaultKerf = it }, label = { Text("Kerf (mm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(minOffcut, { minOffcut = it }, label = { Text("Minimális maradék (mm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = useInventory, onCheckedChange = { useInventory = it })
                Text("Raktárkészlet használata")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(stockLengths, { stockLengths = it }, label = { Text("Szálhosszak (mm, vesszővel)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(onClick = { nav.popBackStack() }) { Text("Mégse") }
        }
    }
}