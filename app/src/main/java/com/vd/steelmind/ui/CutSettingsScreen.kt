package com.vd.steelmind.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private const val PREFS = "vd_cut_settings"

@Composable
fun CutSettingsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var defaultStock by remember { mutableStateOf(prefs.getString("defaultStock", "6000") ?: "6000") }
    var defaultKerf by remember { mutableStateOf((prefs.getInt("defaultKerf", 3)).toString()) }
    var minOffcut by remember { mutableStateOf((prefs.getInt("minOffcut", 200)).toString()) }
    var pdfBars by remember { mutableStateOf(prefs.getBoolean("pdfBars", true)) }

    Scaffold(topBar = { TopAppBar(title = { Text("Darabolás beállítások") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            OutlinedTextField(defaultStock, { defaultStock = it }, label = { Text("Alap szálhossz(ok) mm, vesszővel") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(defaultKerf, { defaultKerf = it }, label = { Text("Kerf alapérték (mm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(minOffcut, { minOffcut = it }, label = { Text("Minimális maradék (mm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = pdfBars, onCheckedChange = { pdfBars = it })
                Text("Színes sávok a PDF-ben")
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                prefs.edit()
                    .putString("defaultStock", defaultStock)
                    .putInt("defaultKerf", defaultKerf.toIntOrNull() ?: 3)
                    .putInt("minOffcut", minOffcut.toIntOrNull() ?: 200)
                    .putBoolean("pdfBars", pdfBars)
                    .apply()
                nav.popBackStack()
            }) { Text("Mentés") }
        }
    }
}