package com.vd.steelmind.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DarabolasMenuScreen(nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Darabolás") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Button(onClick = { nav.navigate("project_new") }, modifier = Modifier.fillMaxWidth()) { Text("Új projekt létrehozása") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { nav.navigate("projects") }, modifier = Modifier.fillMaxWidth()) { Text("Projekt lista") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { nav.navigate("catalog") }, modifier = Modifier.fillMaxWidth()) { Text("Tételkezelő (Ft/m)") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { nav.navigate("inventory") }, modifier = Modifier.fillMaxWidth()) { Text("Raktárkészlet & Maradékok") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { nav.navigate("cut_settings") }, modifier = Modifier.fillMaxWidth()) { Text("Darabolás beállítások") }
        }
    }
}