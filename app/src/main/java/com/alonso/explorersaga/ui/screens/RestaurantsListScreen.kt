package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alonso.explorersaga.R // Importamos R

@Composable
fun RestaurantsListScreen(_navController: NavController) { // Parámetro no usado
    // Datos de ejemplo completos para restaurantes
    val restaurants = listOf(
        Place(
            id = 4,
            name = "Restaurante A de Arco",
            description = "Cocina extremeña moderna junto al Arco de Trajano.",
            category = "restaurante",
            horarios = "13:00 - 16:00, 20:00 - 23:00",
            direccion = "C. Trajano, 5, 06800 Mérida",
            latitude = 38.9175,
            longitude = -6.3458,
            imageResId = R.drawable.teatro_merida // Usamos un placeholder
        ),
        Place(
            id = 5,
            name = "Sybarit",
            description = "Tapas y platos creativos en un ambiente acogedor.",
            category = "restaurante",
            horarios = "12:30 - 16:30, 20:00 - 00:00",
            direccion = "C. John Lennon, 15, 06800 Mérida",
            latitude = 38.915,
            longitude = -6.347,
            imageResId = R.drawable.teatro_merida // Placeholder
        ),
        Place(
            id = 6,
            name = "De Tripas Corazón",
            description = "Gastronomía local con un toque diferente y original.",
            category = "restaurante",
            horarios = "13:30 - 16:00, 20:30 - 23:00",
            direccion = "C. de Arcos, 11, 06800 Mérida",
            latitude = 38.915,
            longitude = -6.346,
            imageResId = R.drawable.teatro_merida // Placeholder
        )
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(restaurants) { place ->
                PlaceListItem(place = place)
            }
        }
    }
}
