package com.alonso.explorersaga.ui.composables

import androidx.annotation.StringRes
import com.alonso.explorersaga.R
import com.alonso.explorersaga.ui.viewmodels.FilterState

// Descripción de los filtros en un solo sitio, compartida por Explorar y la pantalla de filtros del mapa
enum class FilterGroup(@StringRes val label: Int, val preset: FilterState) {
    HERITAGE(R.string.group_heritage, FilterState.HISTORICO),
    FOOD(R.string.group_food, FilterState.GASTRONOMIA),
    SHOPS(R.string.group_shops, FilterState.TIENDAS)
}

enum class FilterOption(
    @StringRes val label: Int,
    val group: FilterGroup,
    val isOn: (FilterState) -> Boolean,
    val set: (FilterState, Boolean) -> FilterState
) {
    MONUMENTS(R.string.filter_monuments, FilterGroup.HERITAGE, { it.monuments }, { s, v -> s.copy(monuments = v) }),
    CHURCHES(R.string.filter_churches, FilterGroup.HERITAGE, { it.iglesias }, { s, v -> s.copy(iglesias = v) }),
    MUSEUMS(R.string.filter_museums, FilterGroup.HERITAGE, { it.museos }, { s, v -> s.copy(museos = v) }),
    RESTAURANTS(R.string.filter_restaurants, FilterGroup.FOOD, { it.restaurants }, { s, v -> s.copy(restaurants = v) }),
    CAFES(R.string.filter_cafes, FilterGroup.FOOD, { it.cafeterias }, { s, v -> s.copy(cafeterias = v) }),
    BARS(R.string.filter_bars, FilterGroup.FOOD, { it.bar }, { s, v -> s.copy(bar = v) }),
    ICE_CREAM(R.string.filter_ice_cream, FilterGroup.FOOD, { it.helados }, { s, v -> s.copy(helados = v) }),
    FOOD_SHOPS(R.string.filter_food_shops, FilterGroup.SHOPS, { it.tiendaAlimentos }, { s, v -> s.copy(tiendaAlimentos = v) }),
    SOUVENIRS(R.string.filter_souvenirs, FilterGroup.SHOPS, { it.souvenirs }, { s, v -> s.copy(souvenirs = v) }),
    SUPERMARKETS(R.string.filter_supermarkets, FilterGroup.SHOPS, { it.supermercados }, { s, v -> s.copy(supermercados = v) }),
    BOOKSHOPS(R.string.filter_bookshops, FilterGroup.SHOPS, { it.libreria }, { s, v -> s.copy(libreria = v) }),
    CLOTHES(R.string.filter_clothes, FilterGroup.SHOPS, { it.ropa }, { s, v -> s.copy(ropa = v) }),
    GENERAL(R.string.filter_general, FilterGroup.SHOPS, { it.tiendasGenerales }, { s, v -> s.copy(tiendasGenerales = v) });

    companion object {
        fun of(group: FilterGroup) = entries.filter { it.group == group }
    }
}

/** El grupo al que pertenecen todos los filtros activos, o null si hay varios grupos o ninguno. */
fun FilterState.singleGroup(): FilterGroup? =
    FilterOption.entries.filter { it.isOn(this) }.map { it.group }.distinct().singleOrNull()

/** Deja activo solo [option] (o todo su grupo si es null). */
fun onlyOption(group: FilterGroup, option: FilterOption?): FilterState {
    val empty = FilterOption.entries.fold(FilterState()) { state, it -> it.set(state, false) }
    return if (option == null) group.preset else option.set(empty, true)
}
