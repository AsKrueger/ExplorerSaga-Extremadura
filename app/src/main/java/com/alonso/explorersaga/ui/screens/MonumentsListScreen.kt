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
fun MonumentsListScreen(navController: NavController) {
    val monuments = listOf(
        Place("Teatro Romano", "Espectacular teatro del siglo I a.C.", "⭐⭐⭐⭐⭐"),
        Place("Anfiteatro Romano", "Lugar de antiguas luchas de gladiadores", "⭐⭐⭐⭐⭐"),
        Place("Acueducto de los Milagros", "Impresionante obra de ingeniería romana", "⭐⭐⭐⭐"),
        Place("Templo de Diana", "Templo de culto imperial muy bien conservado", "⭐⭐⭐⭐")
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(monuments) { place ->
                PlaceListItem(place = place)
            }
        }
    }
}
