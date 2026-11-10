package com.alonso.explorersaga.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.ui.model.PlaceGroup
import com.alonso.explorersaga.ui.model.categoryInfo

@Composable
fun PlaceListItem(place: Place, distanceMeters: Double?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val english = isEnglish()
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlaceThumbnail(place, size = 72.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    place.displayName(english),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${stringResource(categoryInfo(place.category).label)} · ${place.municipality}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    distanceMeters?.let {
                        Icon(Icons.Filled.NearMe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text(formatDistance(it), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    if (place.heritage) {
                        Icon(Icons.Filled.Verified, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                        Text(
                            stringResource(R.string.badge_heritage),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

/** Foto del lugar o, si no tiene (o sin conexión), el icono de su categoría. */
@Composable
fun PlaceThumbnail(place: Place, size: Dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Box(modifier.size(size).clip(shape)) {
        if (place.imageUrl != null) {
            SubcomposeAsyncImage(
                model = place.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { CategoryIconBox(place) },
                error = { CategoryIconBox(place) }
            )
        } else {
            CategoryIconBox(place)
        }
    }
}

@Composable
fun CategoryIconBox(place: Place, modifier: Modifier = Modifier) {
    val color = PlaceGroup.fromKey(place.groupKey)?.color ?: MaterialTheme.colorScheme.primary
    Box(
        modifier.fillMaxSize().background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(categoryInfo(place.category).icon, contentDescription = null, tint = color.darken())
    }
}

private fun Color.darken() = Color(red * 0.8f, green * 0.8f, blue * 0.8f, alpha)
