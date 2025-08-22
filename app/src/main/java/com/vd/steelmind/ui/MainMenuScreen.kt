package com.vd.steelmind.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MainMenuScreen(nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("VD SteelMind") }) }) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { nav.navigate("darabolas") }, modifier = Modifier.fillMaxWidth()) {
                Text("Darabolás")
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { nav.navigate("settings") }, modifier = Modifier.fillMaxWidth()) {
                Text("Beállítások")
            }
        }
    }
}