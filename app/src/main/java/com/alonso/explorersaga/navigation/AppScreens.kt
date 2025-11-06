package com.alonso.explorersaga.navigation

sealed class AppScreens(val route: String) {
    object Home : AppScreens("home")
    object Informacion : AppScreens("informacion")
    object Mapa : AppScreens("mapa")
    object Filtrado : AppScreens("filtrado")
    object Lugares : AppScreens("lugares")
    object PlaceDetail : AppScreens("place_detail")

    // Ruta genérica para todas las listas
    object PlacesList : AppScreens("places_list") 
}
