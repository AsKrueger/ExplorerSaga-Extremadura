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
        composable(AppScreens.Home.route) {
            HomeScreen(onExploreClicked = { navController.navigate(AppScreens.Informacion.route) })
        }
        composable(AppScreens.Informacion.route) {
            InformacionScreen(
                onMapClicked = { navController.navigate(AppScreens.Mapa.route) },
                onPlacesClicked = { navController.navigate(AppScreens.Lugares.route) }
            )
        }
        composable(AppScreens.Mapa.route) {
            MapScreen(
                viewModel = placesViewModel,
                onFilterClicked = { navController.navigate(AppScreens.Filtrado.route) },
                onRoutesClicked = { /* TODO */ },
                onCenterLocationClicked = { /* TODO */ }
            )
        }
        composable(AppScreens.Filtrado.route) {
            FilterScreen(
                viewModel = placesViewModel,
                navController = navController
            )
        }
        
        composable(AppScreens.Lugares.route) {
            PlacesScreen(
                onMonumentsClicked = { navController.navigate(AppScreens.MonumentsList.route) },
                onRestaurantsClicked = { navController.navigate(AppScreens.RestaurantsList.route) },
                onShopsClicked = { navController.navigate(AppScreens.ShopsList.route) }
            )
        }

        // --- CORRECCIÓN FINAL ---
        composable(AppScreens.MonumentsList.route) {
            MonumentsListScreen(viewModel = placesViewModel)
        }
        composable(AppScreens.RestaurantsList.route) {
            RestaurantsListScreen(viewModel = placesViewModel)
        }
        composable(AppScreens.ShopsList.route) {
            ShopsListScreen(viewModel = placesViewModel)
        }
        // ------------------------
    }
}
