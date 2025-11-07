package com.alonso.explorersaga.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alonso.explorersaga.R
import com.alonso.explorersaga.model.Place

@Composable
fun PlaceDetailScreen(place: Place) {
    val context = LocalContext.current
    val imageResId = remember(place.photo) {
        if (place.photo?.startsWith("http") == false) {
            getDrawableResourceId(context, place.photo)
        } else {
            null
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState()) // Hacemos que toda la pantalla sea scrollable
        ) {
            // --- SECCIÓN DE LA IMAGEN (HERO IMAGE) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Hacemos la imagen un poco más grande
            ) {
                // La Imagen
                AsyncImage(
                    model = imageResId ?: place.photo ?: R.drawable.teatro_merida,
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.teatro_merida),
                    error = painterResource(id = R.drawable.teatro_merida)
                )

                // Gradiente oscuro para legibilidad del texto
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = 300f, // Empezar el gradiente a 1/3 de la imagen
                                endY = 800f
                            )
                        )
                )

                // Título del Lugar (superpuesto)
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            // --- SECCIÓN DE CONTENIDO ---
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Descripción
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                HorizontalDivider()

                // Horarios
                place.horarios?.let {
                    InfoRow(label = "Horarios:", value = it)
                }

                // Dirección
                place.direccion?.let {
                    InfoRow(label = "Dirección:", value = it)
                }
            }
        }
    }
}

// Pequeño Composable para mostrar filas de información de forma consistente
@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(90.dp) // Ancho fijo para la etiqueta
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun getDrawableResourceId(context: Context, name: String): Int? {
    val resourceId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (resourceId == 0) null else resourceId
}