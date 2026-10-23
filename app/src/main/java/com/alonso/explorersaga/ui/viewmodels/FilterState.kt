package com.alonso.explorersaga.ui.viewmodels

data class FilterState(
    val monuments: Boolean = true,
    val iglesias: Boolean = false,
    val museos: Boolean = false,
    val restaurants: Boolean = false,
    val cafeterias: Boolean = false,
    val bar: Boolean = false,
    val helados: Boolean = false,
    val tiendaAlimentos: Boolean = false,
    val souvenirs: Boolean = false,
    val supermercados: Boolean = false,
    val libreria: Boolean = false,
    val ropa: Boolean = false,
    val tiendasGenerales: Boolean = false
) {

    // Palabras clave que identifican cada filtro tanto en las categorías locales (assets JSON)
    // como en las del backend (seed de Flyway y categorías normalizadas desde OpenStreetMap)
    fun activeCategoryKeywords(): List<String> = buildList {
        if (monuments) addAll(listOf("monumento", "monument", "historical"))
        if (iglesias) addAll(listOf("iglesia", "religious", "worship"))
        if (museos) addAll(listOf("museo", "museum", "attraction", "tourism"))
        if (restaurants) addAll(listOf("restaurante", "restaurant", "gastronomía"))
        if (cafeterias) addAll(listOf("cafeteria", "cafe"))
        if (bar) addAll(listOf("bar", "pub"))
        if (helados) addAll(listOf("heladeria", "ice_cream"))
        if (tiendaAlimentos) addAll(listOf("tienda_alimentos", "alimentos", "food"))
        if (souvenirs) addAll(listOf("souvenir", "gift"))
        if (supermercados) addAll(listOf("supermercado", "supermarket"))
        if (libreria) addAll(listOf("libreria", "book"))
        if (ropa) addAll(listOf("ropa", "clothes"))
        if (tiendasGenerales) addAll(listOf("tienda_general", "tiendas", "shop", "store"))
    }

    fun matches(category: String): Boolean =
        activeCategoryKeywords().any { keyword -> category.contains(keyword, ignoreCase = true) }

    companion object {
        val ALL = FilterState(
            monuments = true, iglesias = true, museos = true,
            restaurants = true, cafeterias = true, bar = true, helados = true,
            tiendaAlimentos = true, souvenirs = true, supermercados = true,
            libreria = true, ropa = true, tiendasGenerales = true
        )

        val HISTORICO = FilterState(monuments = true, iglesias = true, museos = true)

        val GASTRONOMIA = FilterState(
            monuments = false,
            restaurants = true, cafeterias = true, bar = true, helados = true
        )

        val TIENDAS = FilterState(
            monuments = false,
            tiendaAlimentos = true, souvenirs = true, supermercados = true,
            libreria = true, ropa = true, tiendasGenerales = true
        )
    }
}
