package com.vd.steelmind.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Beállítások (általános)") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Nyelv: Magyar")
            Spacer(Modifier.height(8.dp))
            Text("Téma: Rendszer alapértelmezett")
            Spacer(Modifier.height(8.dp))
            Text("Mentés: helyi / megosztás")
        }
    }
}