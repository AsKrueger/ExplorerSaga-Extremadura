package com.alonso.explorersaga.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alonso.explorersaga.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("informacion") { InformacionScreen(navController) }
        composable("mapa") { MapScreen(navController) }
        composable("filtrado") { FilterScreen(navController) }
        composable("lugares") { PlacesScreen(navController) }
        composable("listado_detallado") { DetailedListScreen(navController) }
    }
}
