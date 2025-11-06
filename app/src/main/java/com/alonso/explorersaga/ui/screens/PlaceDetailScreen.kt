package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonso.explorersaga.model.Place

@Composable
fun PlaceDetailScreen(place: Place) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()) // Para que la pantalla sea scrollable
        ) {
            // Imagen del lugar
            place.imageResId?.let { imageId ->
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = place.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Contenido de texto
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre del lugar
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                // Descripción
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                HorizontalDivider()

                // Horarios
                place.horarios?.let {
                    Text(
                        text = "Horarios: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Dirección
                place.direccion?.let {
                    Text(
                        text = "Dirección: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
