package com.alonso.explorersaga.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alonso.explorersaga.R
import com.alonso.explorersaga.model.Place
import com.alonso.explorersaga.util.MERIDA_CENTER
import com.alonso.explorersaga.util.distanceInMeters

@Composable
fun PlaceListItem(
    place: Place,
    userLocation: Pair<Double, Double> = MERIDA_CENTER,
    onItemClicked: (Place) -> Unit
) {
    val imageModel = rememberPlaceImageModel(place.photo)

    // Cálculo dinámico de distancia desde la ubicación GPS real del usuario
    val distanceText = remember(place.latitude, place.longitude, userLocation) {
        formatDistance(userLocation.first, userLocation.second, place.latitude, place.longitude)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = { onItemClicked(place) }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Imagen en Miniatura
            AsyncImage(
                model = imageModel ?: R.drawable.teatro_merida,
                contentDescription = place.name,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.teatro_merida),
                error = painterResource(id = R.drawable.teatro_merida)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Columna con la Información
            Column(modifier = Modifier.weight(1f)) {
                // Nombre del lugar
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Descripción breve
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Distancia por cercanía GPS real
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Dirección
                place.direccion?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun formatDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
    val meters = distanceInMeters(lat1, lon1, lat2, lon2).toInt()
    return if (meters < 1000) {
        "📍 a $meters m"
    } else {
        "📍 a %.1f km".format(meters / 1000.0)
    }
}
