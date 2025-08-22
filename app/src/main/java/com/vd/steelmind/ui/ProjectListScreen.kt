package com.vd.steelmind.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vd.steelmind.data.AppDatabase
import com.vd.steelmind.data.ProjectEntity
import com.vd.steelmind.util.ProjectExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProjectListScreen(nav: NavController, db: AppDatabase) {
    var projects by remember { mutableStateOf(listOf<ProjectEntity>()) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    fun refresh() { scope.launch(Dispatchers.IO) { projects = db.appDao().listProjects() } }
    LaunchedEffect(Unit) { refresh() }

    Scaffold(topBar = { TopAppBar(title = { Text("Projektek") }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(8.dp)) {
            items(projects) { p ->
                Card(Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f).clickable { nav.navigate("demand/${p.id}") }) {
                                Text(p.name, style = MaterialTheme.typography.titleMedium)
                                Text("Kerf: ${p.kerfMmDefault} mm • Min. maradék: ${p.minOffcutMm} mm")
                                Text(if (p.useInventory) "Mód: Raktárkészlet" else "Mód: Teljes igény")
                            }
                            Button(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val uri = ProjectExporter.exportProject(ctx, db, p.id)
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    ctx.startActivity(Intent.createChooser(share, "Projekt megosztása"))
                                }
                            }) { Text("Export") }
                        }
                    }
                }
            }
        }
    }
}