package com.alonso.explorersaga.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alonso.explorersaga.ExplorerSagaApplication
import com.alonso.explorersaga.ui.screens.*
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 1. Obtenemos el contexto y, a través de él, nuestro AppContainer.
    val appContainer = (LocalContext.current.applicationContext as ExplorerSagaApplication).container

    // 2. Creamos una única instancia del ViewModel, pidiéndole la factory al AppContainer.
    val placesViewModel: PlacesViewModel = viewModel(factory = appContainer.placesViewModelFactory)

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("informacion") { InformacionScreen(navController) }
        composable("mapa") { MapScreen(navController) }
        composable("filtrado") { FilterScreen(navController) }
        composable("lugares") { PlacesScreen(navController) }

        // 3. Pasamos el ViewModel a las tres pantallas que lo necesitan.
        composable("monuments_list") {
            MonumentsListScreen(navController, placesViewModel)
        }
        composable("restaurants_list") {
            RestaurantsListScreen(navController, placesViewModel)
        }
        composable("shops_list") {
            ShopsListScreen(navController, placesViewModel)
        }
    }
}
