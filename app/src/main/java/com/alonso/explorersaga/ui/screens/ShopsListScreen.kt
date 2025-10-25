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
import kotlin.Suppress

@Composable
@Suppress("UNUSED_PARAMETER")
fun ShopsListScreen(_navController: NavController) {
    val shops = listOf(
        Place("Terracota", "Artesanía y cerámica típica de la región", "⭐⭐⭐⭐"),
        Place("Emérita Souvenirs", "Recuerdos variados de la Mérida romana", "⭐⭐⭐")
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
