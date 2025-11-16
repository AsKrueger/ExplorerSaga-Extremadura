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
        // ... (otras rutas sin cambios)
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
                onCenterLocationClicked = { /* TODO */ },
                onInfoClicked = { navController.navigate(AppScreens.Informacion.route) },
                onPlacesClicked = { navController.navigate(AppScreens.Lugares.route) }
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
                    // Lógica de filtrado actualizada para TODAS las categorías
                    val newState = when (category) {
                        "historico" -> FilterState(monuments = true, iglesias = true, museos = true, restaurants = false, cafeterias = false, bar = false, helados = false, tiendaAlimentos = false, souvenirs = false, supermercados = false, libreria = false, ropa = false, tiendasGenerales = false)
                        "gastronomia" -> FilterState(monuments = false, iglesias = false, museos = false, restaurants = true, cafeterias = true, bar = true, helados = true, tiendaAlimentos = false, souvenirs = false, supermercados = false, libreria = false, ropa = false, tiendasGenerales = false)
                        "tienda" -> FilterState(monuments = false, iglesias = false, museos = false, restaurants = false, cafeterias = false, bar = false, helados = false, tiendaAlimentos = true, souvenirs = true, supermercados = true, libreria = true, ropa = true, tiendasGenerales = true)
                        else -> FilterState(monuments=true, iglesias=true, museos=true, restaurants=true, cafeterias=true, bar = true, helados = true, tiendaAlimentos = true, souvenirs = true, supermercados = true, libreria = true, ropa = true, tiendasGenerales = true) // Estado por defecto: todo activo
                    }
                    placesViewModel.updateFilters(newState)
                    
                    navController.navigate(AppScreens.PlacesList.route) 
                },
                onMapClicked = { navController.navigate(AppScreens.Mapa.route) },
                onInfoClicked = { navController.navigate(AppScreens.Informacion.route) }
            )
        }

        composable(AppScreens.PlacesList.route) {
            PlacesListScreen(navController = navController, viewModel = placesViewModel)
        }

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
