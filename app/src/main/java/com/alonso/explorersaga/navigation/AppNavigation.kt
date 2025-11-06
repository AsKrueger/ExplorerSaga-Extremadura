package com.alonso.explorersaga.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alonso.explorersaga.ExplorerSagaApplication
import com.alonso.explorersaga.ui.screens.*
import com.alonso.explorersaga.ui.viewmodels.FilterState
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val appContainer = (LocalContext.current.applicationContext as ExplorerSagaApplication).container
    val placesViewModel: PlacesViewModel = viewModel(factory = appContainer.placesViewModelFactory)

    NavHost(navController = navController, startDestination = AppScreens.Home.route) {
        // ... (rutas Home, Informacion, Mapa, Filtrado)
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
                onCategorySelected = { category ->
                    val newState = when (category) {
                        "monumento" -> FilterState(monuments = true, restaurants = false, shops = false)
                        "restaurante" -> FilterState(monuments = false, restaurants = true, shops = false)
                        "tienda" -> FilterState(monuments = false, restaurants = false, shops = true)
                        else -> FilterState()
                    }
                    placesViewModel.updateFilters(newState)
                    
                    // Navegamos a la nueva pantalla de lista genérica
                    navController.navigate(AppScreens.PlacesList.route) 
                }
            )
        }

        // --- PANTALLA DE LISTA ÚNICA ---
        composable(AppScreens.PlacesList.route) {
            PlacesListScreen(navController = navController, viewModel = placesViewModel)
        }

        // --- PANTALLA DE DETALLE ---
        composable(
            route = "${AppScreens.PlaceDetail.route}/{placeId}",
            arguments = listOf(navArgument("placeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getInt("placeId")
            if (placeId != null) {
                LaunchedEffect(placeId) {
                    placesViewModel.loadPlaceById(placeId)
                }
                val uiState by placesViewModel.uiState.collectAsState()
                uiState.selectedPlace?.let { selectedPlace ->
                    PlaceDetailScreen(place = selectedPlace)
                }
            }
        }
    }
}
