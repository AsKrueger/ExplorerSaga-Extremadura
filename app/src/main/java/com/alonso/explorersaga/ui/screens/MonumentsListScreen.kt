package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

@Composable
fun MonumentsListScreen(
    _navController: NavController, // Lo dejamos sin usar por ahora
    viewModel: PlacesViewModel // 1. Recibimos el ViewModel como parámetro
) {
    // 2. Observamos el estado del ViewModel. Cada vez que cambie, la UI se recompone.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 3. Este efecto se lanza una sola vez cuando la pantalla aparece.
    LaunchedEffect(key1 = Unit) {
        // 4. Le pedimos al ViewModel que cargue los monumentos.
        viewModel.loadPlaces("monumento")
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 5. Usamos la lista de lugares del ViewModel, no la lista de ejemplo.
            items(uiState.places) { place ->
                PlaceListItem(place = place)
            }
        }
    }
}
