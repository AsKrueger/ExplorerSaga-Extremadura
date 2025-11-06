package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Lógica para mostrar los filtros adecuados según la categoría
            when {
                // Caso para lugares históricos
                filterState.monuments || filterState.iglesias || filterState.museos -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(selected = filterState.monuments && filterState.iglesias && filterState.museos, onClick = { viewModel.updateFilters(filterState.copy(monuments = true, iglesias = true, museos = true)) }, label = { Text("Todos") })
                        FilterChip(selected = filterState.monuments && !filterState.iglesias && !filterState.museos, onClick = { viewModel.updateFilters(filterState.copy(monuments = true, iglesias = false, museos = false)) }, label = { Text("Monumentos") })
                        FilterChip(selected = !filterState.monuments && filterState.iglesias && !filterState.museos, onClick = { viewModel.updateFilters(filterState.copy(monuments = false, iglesias = true, museos = false)) }, label = { Text("Iglesias") })
                        FilterChip(selected = !filterState.monuments && !filterState.iglesias && filterState.museos, onClick = { viewModel.updateFilters(filterState.copy(monuments = false, iglesias = false, museos = true)) }, label = { Text("Museos") })
                    }
                }
                // Caso para gastronomía
                filterState.restaurants || filterState.cafeterias -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(selected = filterState.restaurants && filterState.cafeterias, onClick = { viewModel.updateFilters(filterState.copy(restaurants = true, cafeterias = true)) }, label = { Text("Todos") })
                        FilterChip(selected = filterState.restaurants && !filterState.cafeterias, onClick = { viewModel.updateFilters(filterState.copy(restaurants = true, cafeterias = false)) }, label = { Text("Restaurantes") })
                        FilterChip(selected = !filterState.restaurants && filterState.cafeterias, onClick = { viewModel.updateFilters(filterState.copy(restaurants = false, cafeterias = true)) }, label = { Text("Cafeterías") })
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.places) { place ->
                    PlaceListItem(place = place, onItemClicked = {
                        navController.navigate("${AppScreens.PlaceDetail.route}/${place.id}")
                    })
                }
            }
        }
    }
}
