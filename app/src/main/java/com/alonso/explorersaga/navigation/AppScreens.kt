package com.alonso.explorersaga.navigation

sealed class AppScreens(val route: String) {
    object Home : AppScreens("home")
    object Informacion : AppScreens("informacion")
    object Mapa : AppScreens("mapa")
    object Filtrado : AppScreens("filtrado")
    object Lugares : AppScreens("lugares")
    object MonumentsList : AppScreens("monuments_list")
    object RestaurantsList : AppScreens("restaurants_list")
    object ShopsList : AppScreens("shops_list")
}
