package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alonso.explorersaga.navigation.AppScreens
import com.alonso.explorersaga.ui.composables.PlaceListItem
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

@Composable
fun PlacesListScreen(
    navController: NavController,
    viewModel: PlacesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState = uiState.filterState
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Lógica para mostrar los filtros adecuados según la categoría
            when {
                // Caso para lugares históricos
                filterState.monuments || filterState.iglesias || filterState.museos -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { FilterChip(selected = filterState.monuments && filterState.iglesias && filterState.museos, onClick = { viewModel.updateFilters(filterState.copy(monuments = true, iglesias = true, museos = true)) }, label = { Text("Todos") }) }
                        item { FilterChip(selected = filterState.monuments && !filterState.iglesias && !filterState.museos, onClick = { viewModel.updateFilters(filterState.copy(monuments = true, iglesias = false, museos = false)) }, label = { Text("Monumentos") }) }
                        item { FilterChip(selected = !filterState.monuments && filterState.iglesias && !filterState.museos, onClick = { viewModel.updateFilters(filterState.copy(monuments = false, iglesias = true, museos = false)) }, label = { Text("Iglesias") }) }
                        item { FilterChip(selected = !filterState.monuments && !filterState.iglesias && filterState.museos, onClick = { viewModel.updateFilters(filterState.copy(monuments = false, iglesias = false, museos = true)) }, label = { Text("Museos") }) }
                    }
                }
                // Caso para gastronomía
                filterState.restaurants || filterState.cafeterias || filterState.bar || filterState.helados -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { FilterChip(selected = filterState.restaurants && filterState.cafeterias && filterState.bar && filterState.helados, onClick = { viewModel.updateFilters(filterState.copy(restaurants = true, cafeterias = true, bar = true, helados = true)) }, label = { Text("Todos") }) }
                        item { FilterChip(selected = filterState.restaurants && !filterState.cafeterias && !filterState.bar && !filterState.helados, onClick = { viewModel.updateFilters(filterState.copy(restaurants = true, cafeterias = false, bar = false, helados = false)) }, label = { Text("Restaurantes") }) }
                        item { FilterChip(selected = !filterState.restaurants && filterState.cafeterias && !filterState.bar && !filterState.helados, onClick = { viewModel.updateFilters(filterState.copy(restaurants = false, cafeterias = true, bar = false, helados = false)) }, label = { Text("Cafeterías") }) }
                        item { FilterChip(selected = !filterState.restaurants && !filterState.cafeterias && filterState.bar && !filterState.helados, onClick = { viewModel.updateFilters(filterState.copy(restaurants = false, cafeterias = false, bar = true, helados = false)) }, label = { Text("Bar") }) }
                        item { FilterChip(selected = !filterState.restaurants && !filterState.cafeterias && !filterState.bar && filterState.helados, onClick = { viewModel.updateFilters(filterState.copy(restaurants = false, cafeterias = false, bar = false, helados = true)) }, label = { Text("Helados") }) }
                    }
                }
                 // --- NUEVO: Caso para Tiendas ---
                filterState.tiendaAlimentos || filterState.souvenirs || filterState.supermercados || filterState.libreria || filterState.ropa || filterState.tiendasGenerales -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { FilterChip(selected = filterState.tiendaAlimentos && filterState.souvenirs && filterState.supermercados && filterState.libreria && filterState.ropa && filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = true, souvenirs = true, supermercados = true, libreria = true, ropa = true, tiendasGenerales = true)) }, label = { Text("Todas") }) }
                        item { FilterChip(selected = filterState.tiendaAlimentos && !filterState.souvenirs && !filterState.supermercados && !filterState.libreria && !filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = true, souvenirs = false, supermercados = false, libreria = false, ropa = false, tiendasGenerales = false)) }, label = { Text("Alimentos") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && filterState.souvenirs && !filterState.supermercados && !filterState.libreria && !filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = true, supermercados = false, libreria = false, ropa = false, tiendasGenerales = false)) }, label = { Text("Souvenirs") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && !filterState.souvenirs && filterState.supermercados && !filterState.libreria && !filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = false, supermercados = true, libreria = false, ropa = false, tiendasGenerales = false)) }, label = { Text("Supermercados") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && !filterState.souvenirs && !filterState.supermercados && filterState.libreria && !filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = false, supermercados = false, libreria = true, ropa = false, tiendasGenerales = false)) }, label = { Text("Libreria") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && !filterState.souvenirs && !filterState.supermercados && !filterState.libreria && filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = false, supermercados = false, libreria = false, ropa = true, tiendasGenerales = false)) }, label = { Text("Ropa") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && !filterState.souvenirs && !filterState.supermercados && !filterState.libreria && !filterState.ropa && filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = false, supermercados = false, libreria = false, ropa = false, tiendasGenerales = true)) }, label = { Text("General") }) }
                    }
                }
            }

            val filteredPlaces = uiState.places.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredPlaces) { place ->
                    PlaceListItem(place = place, onItemClicked = {
                        navController.navigate("${AppScreens.PlaceDetail.route}/${place.id}")
                    })
                }
            }
        }
    }
}