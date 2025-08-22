package com.vd.steelmind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.vd.steelmind.data.AppDatabase
import com.vd.steelmind.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "vd-steelmind.db"
        ).fallbackToDestructiveMigration().build()

        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = "menu") {
                    composable("menu") { MainMenuScreen(nav) }
                    composable("settings") { SettingsScreen(nav) }
                    composable("darabolas") { DarabolasMenuScreen(nav) }
                    composable("cut_settings") { CutSettingsScreen(nav) }
                    composable("project_new") { ProjectEditorScreen(nav, db, projectId = null) }
                    composable("projects") { ProjectListScreen(nav, db) }
                    composable("catalog") { ItemCatalogScreen(nav, db) }
                    composable("demand/{projectId}") { backStackEntry ->
                        val pid = backStackEntry.arguments?.getString("projectId")?.toLong() ?: 0L
                        DemandScreen(nav, db, pid)
                    }
                    composable("plan/{projectId}") { backStackEntry ->
                        val pid = backStackEntry.arguments?.getString("projectId")?.toLong() ?: 0L
                        PlanScreen(nav, db, pid)
                    }
                    composable("inventory") { InventoryScreen(nav, db) }
                }
            }
        }
    }
}