package com.alonso.explorersaga.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.alonso.explorersaga.R
import com.alonso.explorersaga.model.Place

// Composable reutilizable para mostrar un elemento en una lista.
// Ahora acepta un NavController y es más robusto.
@Composable
fun PlaceListItem(place: Place, navController: NavController) {
    val context = LocalContext.current
    val imageModel = remember(place.photo) {
        if (place.photo?.startsWith("http") == false) {
            getDrawableResourceId(context, place.photo)
        } else {
            place.photo
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Implementar navegación a la pantalla de detalle, p. ej. navController.navigate("detail/${place.id}") */ },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de portada (con manejo de nulos y carga asíncrona)
            AsyncImage(
                model = imageModel ?: R.mipmap.ic_launcher,
                contentDescription = place.name,
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f), // Mantiene la imagen cuadrada
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.mipmap.ic_launcher),
                error = painterResource(id = R.mipmap.ic_launcher)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = place.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                // Dirección y horario (con manejo de nulos)
                Text(text = place.direccion ?: "Dirección no disponible", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Horario: ${place.horarios ?: "No disponible"}", fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

private fun getDrawableResourceId(context: Context, name: String): Int? {
    val resourceId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (resourceId == 0) null else resourceId
}
