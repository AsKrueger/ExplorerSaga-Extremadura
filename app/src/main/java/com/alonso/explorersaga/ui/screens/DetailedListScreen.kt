package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class Place(val name: String, val description: String, val rating: String)

@Composable
fun DetailedListScreen(navController: NavController) {
    val places = listOf(
        Place("Teatro Romano", "Espectacular teatro del siglo I a.C.", "⭐⭐⭐⭐⭐"),
        Place("Anfiteatro Romano", "Lugar de antiguas luchas de gladiadores", "⭐⭐⭐⭐⭐"),
        Place("Acueducto de los Milagros", "Impresionante obra de ingeniería romana", "⭐⭐⭐⭐"),
        Place("Templo de Diana", "Templo de culto imperial muy bien conservado", "⭐⭐⭐⭐"),
        Place("Restaurante A de Arco", "Cocina extremeña moderna junto al Arco de Trajano", "⭐⭐⭐⭐⭐")
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(places) { place ->
                PlaceListItem(place = place)
            }
        }
    }
}

@Composable
fun PlaceListItem(place: Place) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder para la imagen
            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                Text(text = "📷", fontSize = 30.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = place.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = place.description, fontSize = 14.sp, color = Color.Gray)
                Text(text = place.rating, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
