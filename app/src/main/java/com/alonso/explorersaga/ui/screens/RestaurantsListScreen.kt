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

@Composable
fun RestaurantsListScreen(navController: NavController) {
    val restaurants = listOf(
        Place("Restaurante A de Arco", "Cocina extremeña moderna junto al Arco de Trajano", "⭐⭐⭐⭐⭐"),
        Place("Sybarit", "Tapas y platos creativos en un ambiente acogedor", "⭐⭐⭐⭐"),
        Place("De Tripas Corazón", "Gastronomía local con un toque diferente", "⭐⭐⭐⭐")
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
