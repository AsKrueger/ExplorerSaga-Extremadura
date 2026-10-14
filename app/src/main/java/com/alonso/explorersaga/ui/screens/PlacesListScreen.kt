package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alonso.explorersaga.data.DataSourceOrigin
import com.alonso.explorersaga.navigation.AppScreens
import com.alonso.explorersaga.ui.composables.PlaceListItem
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

@Composable
fun PlacesListScreen(
    navController: NavController,
    viewModel: PlacesViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterState = uiState.filterState

    // Solicitar ubicación GPS real del dispositivo al cargar la pantalla
    LaunchedEffect(Unit) {
        viewModel.fetchDeviceLocation(context)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // DataSource origin indicator badge
            val originText = when (uiState.dataSourceOrigin) {
                DataSourceOrigin.REMOTE -> "🟢 Conectado al Backend"
                DataSourceOrigin.LOCAL_FALLBACK -> "🟡 Modo Local (Respaldo)"
                else -> null
            }
            originText?.let { text ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.dataSourceOrigin == DataSourceOrigin.REMOTE) Color(0xFF008000) else Color(0xFFB8860B)
                    )
                    if (uiState.dataSourceOrigin == DataSourceOrigin.REMOTE) {
                        OutlinedButton(
                            onClick = { viewModel.importFromWeb() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("🌐 Buscar en la Web", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Búsqueda interactiva en tiempo real ("Live Web Search")
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Buscar por nombre, dirección o descripción...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

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
                // Caso para Tiendas
                filterState.tiendaAlimentos || filterState.souvenirs || filterState.supermercados || filterState.libreria || filterState.ropa || filterState.tiendasGenerales -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { FilterChip(selected = filterState.tiendaAlimentos && filterState.souvenirs && filterState.supermercados && filterState.libreria && filterState.ropa && filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = true, souvenirs = true, supermercados = true, libreria = true, ropa = true, tiendasGenerales = true)) }, label = { Text("Todas") }) }
                        item { FilterChip(selected = filterState.tiendaAlimentos && !filterState.souvenirs && !filterState.supermercados && !filterState.libreria && !filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = true, souvenirs = false, supermercados = false, libreria = false, ropa = false, tiendasGenerales = false)) }, label = { Text("Alimentos") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && filterState.souvenirs && !filterState.supermercados && !filterState.libreria && !filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = true, supermercados = false, libreria = false, ropa = false, tiendasGenerales = false)) }, label = { Text("Souvenirs") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && !filterState.souvenirs && filterState.supermercados && filterState.libreria && !filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = false, supermercados = true, libreria = false, ropa = false, tiendasGenerales = false)) }, label = { Text("Supermercados") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && !filterState.souvenirs && !filterState.supermercados && filterState.libreria && !filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = false, supermercados = false, libreria = true, ropa = false, tiendasGenerales = false)) }, label = { Text("Librería") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && !filterState.souvenirs && !filterState.supermercados && !filterState.libreria && filterState.ropa && !filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = false, supermercados = false, libreria = false, ropa = true, tiendasGenerales = false)) }, label = { Text("Ropa") }) }
                        item { FilterChip(selected = !filterState.tiendaAlimentos && !filterState.souvenirs && !filterState.supermercados && !filterState.libreria && !filterState.ropa && filterState.tiendasGenerales, onClick = { viewModel.updateFilters(filterState.copy(tiendaAlimentos = false, souvenirs = false, supermercados = false, libreria = false, ropa = false, tiendasGenerales = true)) }, label = { Text("General") }) }
                    }
                }
            }

            if (uiState.places.isEmpty() && !uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No se encontraron lugares en la base de datos.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Button(onClick = { viewModel.importFromWeb() }) {
                            Text("🌐 Buscar en la Web (OpenStreetMap)")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { viewModel.retry() }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.places) { place ->
                        PlaceListItem(
                            place = place,
                            userLocation = uiState.userLocation,
                            onItemClicked = {
                                navController.navigate("${AppScreens.PlaceDetail.route}/${place.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}
