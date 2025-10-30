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
import com.alonso.explorersaga.R // ¡Importante! Necesitamos importar R

@Composable
fun MonumentsListScreen(_navController: NavController) { // Parámetro no usado
    // Datos de ejemplo actualizados con la nueva estructura de Place
    val monuments = listOf(
        Place(
            id = 1,
            name = "Teatro Romano",
            description = "Espectacular teatro del siglo I a.C. usado para representaciones teatrales.",
            category = "monumento",
            horarios = "10:00 - 18:00",
            direccion = "Plaza Margarita Xirgú, s/n, 06800 Mérida",
            latitude = 38.9157,
            longitude = -6.3386,
            imageResId = R.drawable.teatro_merida // Usamos un drawable real
        ),
        Place(
            id = 2,
            name = "Anfiteatro Romano",
            description = "Lugar de antiguas luchas de gladiadores y espectáculos públicos.",
            category = "monumento",
            horarios = "10:00 - 18:00",
            direccion = "Plaza Margarita Xirgú, s/n, 06800 Mérida",
            latitude = 38.9165,
            longitude = -6.3375,
            imageResId = R.drawable.teatro_merida // Placeholder
        ),
        Place(
            id = 3,
            name = "Acueducto de los Milagros",
            description = "Impresionante obra de ingeniería romana para traer agua a la ciudad.",
            category = "monumento",
            horarios = "Abierto 24h",
            direccion = "Av. Vía de la Plata, 06800 Mérida",
            latitude = 38.923,
            longitude = -6.348,
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
            items(monuments) { place ->
                PlaceListItem(place = place)
            }
        }
    }
}
