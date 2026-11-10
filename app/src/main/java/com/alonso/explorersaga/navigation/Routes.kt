package com.alonso.explorersaga.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.alonso.explorersaga.R
import kotlinx.serialization.Serializable

// Pestañas de la barra inferior
@Serializable data object HomeRoute
@Serializable data object ExploreRoute
@Serializable data object MapRoute
@Serializable data object TripRoute
@Serializable data object SettingsRoute

// Pantallas secundarias
@Serializable data class PlaceDetailRoute(val placeId: String)
@Serializable data class RouteDetailRoute(val routeId: String)
@Serializable data object RoutesRoute
@Serializable data object AgendaRoute
@Serializable data object ServicesRoute
@Serializable data object FeedbackRoute
@Serializable data object AboutRoute

enum class TopLevelTab(
    val route: Any,
    @StringRes val label: Int,
    val selectedIcon: ImageVector,
    val icon: ImageVector
) {
    HOME(HomeRoute, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    EXPLORE(ExploreRoute, R.string.nav_explore, Icons.Filled.Explore, Icons.Outlined.Explore),
    MAP(MapRoute, R.string.nav_map, Icons.Filled.Map, Icons.Outlined.Map),
    TRIP(TripRoute, R.string.nav_trip, Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks),
    SETTINGS(SettingsRoute, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
}
