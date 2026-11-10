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
import com.alonso.explorersaga.ui.screens.AgendaScreen
import com.alonso.explorersaga.ui.screens.ExploreScreen
import com.alonso.explorersaga.ui.screens.FeedbackScreen
import com.alonso.explorersaga.ui.screens.HomeScreen
import com.alonso.explorersaga.ui.screens.MapScreen
import com.alonso.explorersaga.ui.screens.PlaceDetailScreen
import com.alonso.explorersaga.ui.screens.RouteDetailScreen
import com.alonso.explorersaga.ui.screens.RoutesScreen
import com.alonso.explorersaga.ui.screens.ServicesScreen
import com.alonso.explorersaga.ui.screens.SettingsScreen
import com.alonso.explorersaga.ui.screens.TripScreen
import com.alonso.explorersaga.ui.screens.WelcomeScreen
import com.alonso.explorersaga.ui.viewmodels.ExploreViewModel
import com.alonso.explorersaga.ui.viewmodels.HomeViewModel
import com.alonso.explorersaga.ui.viewmodels.PlaceDetailViewModel
import com.alonso.explorersaga.ui.viewmodels.RouteViewModel
import com.alonso.explorersaga.ui.viewmodels.ServicesViewModel
import com.alonso.explorersaga.ui.viewmodels.SettingsViewModel
import com.alonso.explorersaga.ui.viewmodels.TripViewModel
import kotlinx.coroutines.launch

@Composable
fun ExplorerSagaRoot(container: AppContainer, settings: AppSettings) {
    val scope = rememberCoroutineScope()
    if (!settings.welcomeSeen) {
        WelcomeScreen(onStart = { scope.launch { container.settingsStore.setWelcomeSeen(true) } })
        return
    }

    val navController = rememberNavController()
    val factory = container.viewModelFactory
    // Explorar y el mapa comparten filtros y resultados; Inicio y la agenda, la temporada
    val exploreViewModel: ExploreViewModel = viewModel(factory = factory)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val currentTab = TopLevelTab.entries.firstOrNull { tab ->
        destination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
    }

    val openPlace: (String) -> Unit = { navController.navigate(PlaceDetailRoute(it)) }
    val showOnMap: (String) -> Unit = { placeId ->
        exploreViewModel.focusOnMap(placeId)
        navController.navigateToTab(TopLevelTab.MAP)
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
                    viewModel = homeViewModel,
                    onGroupSelected = { group ->
                        exploreViewModel.showGroup(group)
                        navController.navigateToTab(TopLevelTab.EXPLORE)
                    },
                    onOpenPlace = openPlace,
                    onOpenRoute = { navController.navigate(RouteDetailRoute(it)) },
                    onOpenRoutes = { navController.navigate(RoutesRoute) },
                    onOpenAgenda = { navController.navigate(AgendaRoute) },
                    onOpenServices = { navController.navigate(ServicesRoute) },
                    onOpenMap = { navController.navigateToTab(TopLevelTab.MAP) }
                )
            }
            composable<ExploreRoute> {
                ExploreScreen(viewModel = exploreViewModel, onOpenPlace = openPlace)
            }
            composable<MapRoute> {
                MapScreen(viewModel = exploreViewModel, darkTheme = settings.isDark(), onOpenPlace = openPlace)
            }
            composable<TripRoute> {
                val tripViewModel: TripViewModel = viewModel(factory = factory)
                TripScreen(
                    viewModel = tripViewModel,
                    onOpenPlace = openPlace,
                    onExplore = { navController.navigateToTab(TopLevelTab.EXPLORE) },
                    onOpenRoutes = { navController.navigate(RoutesRoute) }
                )
            }
            composable<SettingsRoute> {
                val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(
                    viewModel = settingsViewModel,
                    darkTheme = settings.isDark(),
                    onOpenFeedback = { navController.navigate(FeedbackRoute) },
                    onOpenAbout = { navController.navigate(AboutRoute) }
                )
            }
            composable<PlaceDetailRoute> { entry ->
                val placeId = entry.toRoute<PlaceDetailRoute>().placeId
                val detailViewModel: PlaceDetailViewModel =
                    viewModel(key = "place-$placeId", factory = container.placeDetailFactory(placeId))
                PlaceDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                    onShowOnMap = showOnMap
                )
            }
            composable<RoutesRoute> {
                RoutesScreen(
                    viewModel = homeViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenRoute = { navController.navigate(RouteDetailRoute(it)) }
                )
            }
            composable<RouteDetailRoute> { entry ->
                val routeId = entry.toRoute<RouteDetailRoute>().routeId
                val routeViewModel: RouteViewModel =
                    viewModel(key = "route-$routeId", factory = container.routeFactory(routeId))
                RouteDetailScreen(
                    viewModel = routeViewModel,
                    darkTheme = settings.isDark(),
                    onBack = { navController.popBackStack() },
                    onOpenPlace = openPlace
                )
            }
            composable<AgendaRoute> {
                AgendaScreen(viewModel = homeViewModel, onBack = { navController.popBackStack() })
            }
            composable<ServicesRoute> {
                val servicesViewModel: ServicesViewModel = viewModel(factory = factory)
                ServicesScreen(
                    viewModel = servicesViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenPlace = openPlace
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

@Composable
private fun AppSettings.isDark(): Boolean = when (themeMode) {
    com.alonso.explorersaga.data.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    com.alonso.explorersaga.data.ThemeMode.LIGHT -> false
    com.alonso.explorersaga.data.ThemeMode.DARK -> true
}

// Navegación estándar entre pestañas: conserva el estado de cada una y no apila pestañas repetidas
private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
