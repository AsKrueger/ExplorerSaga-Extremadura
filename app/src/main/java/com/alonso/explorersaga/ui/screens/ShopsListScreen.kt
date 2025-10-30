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
import com.alonso.explorersaga.R

@Composable
fun ShopsListScreen(_navController: NavController) { // Parámetro no usado
    // Datos de ejemplo completos para tiendas
    val shops = listOf(
        Place(
            id = 7,
            name = "Terracota Mérida",
            description = "Artesanía y cerámica típica de la región.",
            category = "tienda",
            horarios = "10:00 - 14:00, 17:00 - 20:00",
            direccion = "C. José Ramón Mélida, 30, 06800 Mérida",
            latitude = 38.916,
            longitude = -6.339,
            imageResId = R.drawable.teatro_merida // Placeholder
        ),
        Place(
            id = 8,
            name = "Emérita Souvenirs",
            description = "Recuerdos variados de la Mérida romana y de Extremadura.",
            category = "tienda",
            horarios = "09:30 - 21:00",
            direccion = "C. José Ramón Mélida, 15, 06800 Mérida",
            latitude = 38.917,
            longitude = -6.338,
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
            items(shops) { place ->
                PlaceListItem(place = place)
            }
        }
    }
}
