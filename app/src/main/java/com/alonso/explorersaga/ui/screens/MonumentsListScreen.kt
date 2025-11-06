package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

@Composable
fun MonumentsListScreen(
    navController: NavController,
    viewModel: PlacesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Ya no se necesita el LaunchedEffect, la pantalla simplemente
    // muestra el estado actual del ViewModel, que es controlado por los filtros.

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.places) { place ->
                PlaceListItem(place = place, navController = navController)
            }
        }
    }
}
