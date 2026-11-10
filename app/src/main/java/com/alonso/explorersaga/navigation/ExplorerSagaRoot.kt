package com.alonso.explorersaga.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.alonso.explorersaga.AppContainer
import com.alonso.explorersaga.data.AppSettings
import com.alonso.explorersaga.ui.screens.AboutScreen
import com.alonso.explorersaga.ui.screens.ExploreScreen
import com.alonso.explorersaga.ui.screens.FeedbackScreen
import com.alonso.explorersaga.ui.screens.FilterScreen
import com.alonso.explorersaga.ui.screens.HomeScreen
import com.alonso.explorersaga.ui.screens.MapScreen
import com.alonso.explorersaga.ui.screens.PlaceDetailScreen
import com.alonso.explorersaga.ui.screens.SettingsScreen
import com.alonso.explorersaga.ui.screens.WelcomeScreen
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel
import com.alonso.explorersaga.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun ExplorerSagaRoot(container: AppContainer, settings: AppSettings) {
    val scope = rememberCoroutineScope()
    if (!settings.welcomeSeen) {
        WelcomeScreen(onStart = { scope.launch { container.settingsStore.setWelcomeSeen(true) } })
        return
    }

    val navController = rememberNavController()
    val placesViewModel: PlacesViewModel = viewModel(factory = container.viewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = container.viewModelFactory)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val currentTab = TopLevelTab.entries.firstOrNull { tab ->
        destination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
    }

    Scaffold(
        bottomBar = {
            // La barra solo se muestra en las pestañas: el detalle y los formularios usan toda la pantalla
            if (currentTab != null) {
                NavigationBar {
                    TopLevelTab.entries.forEach { tab ->
                        val selected = tab == currentTab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateToTab(tab) },
                            icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onCategorySelected = { filter ->
                        placesViewModel.updateFilters(filter)
                        navController.navigateToTab(TopLevelTab.EXPLORE)
                    },
                    onOpenMap = { navController.navigateToTab(TopLevelTab.MAP) }
                )
            }
            composable<ExploreRoute> {
                ExploreScreen(
                    viewModel = placesViewModel,
                    onPlaceClicked = { navController.navigate(PlaceDetailRoute(it.id)) }
                )
            }
            composable<MapRoute> {
                MapScreen(
                    viewModel = placesViewModel,
                    onFilterClicked = { navController.navigate(FiltersRoute) },
                    onOpenDetail = { navController.navigate(PlaceDetailRoute(it.id)) }
                )
            }
            composable<SettingsRoute> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onOpenFeedback = { navController.navigate(FeedbackRoute) },
                    onOpenAbout = { navController.navigate(AboutRoute) }
                )
            }
            composable<FiltersRoute> {
                FilterScreen(viewModel = placesViewModel, onApply = { navController.popBackStack() })
            }
            composable<PlaceDetailRoute> { entry ->
                PlaceDetailScreen(
                    placeId = entry.toRoute<PlaceDetailRoute>().placeId,
                    viewModel = placesViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<FeedbackRoute> {
                FeedbackScreen(onBack = { navController.popBackStack() })
            }
            composable<AboutRoute> {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

// Navegación estándar entre pestañas: conserva el estado de cada una y no apila pestañas repetidas
private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
