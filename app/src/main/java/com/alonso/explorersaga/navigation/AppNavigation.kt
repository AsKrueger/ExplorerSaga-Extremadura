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

    val appContainer = (LocalContext.current.applicationContext as ExplorerSagaApplication).container
    val placesViewModel: PlacesViewModel = viewModel(factory = appContainer.placesViewModelFactory)

    NavHost(navController = navController, startDestination = AppScreens.Home.route) {
        composable(AppScreens.Home.route) { HomeScreen(navController) }
        composable(AppScreens.Informacion.route) { InformacionScreen(navController) }
        composable(AppScreens.Mapa.route) { MapScreen(navController, placesViewModel) }
        composable(AppScreens.Filtrado.route) { FilterScreen(navController, placesViewModel) }
        composable(AppScreens.Lugares.route) { PlacesScreen(navController) }

        composable(AppScreens.MonumentsList.route) {
            MonumentsListScreen(navController, placesViewModel)
        }
        composable(AppScreens.RestaurantsList.route) {
            RestaurantsListScreen(navController, placesViewModel)
        }
        composable(AppScreens.ShopsList.route) {
            ShopsListScreen(navController, placesViewModel)
        }
    }
}
