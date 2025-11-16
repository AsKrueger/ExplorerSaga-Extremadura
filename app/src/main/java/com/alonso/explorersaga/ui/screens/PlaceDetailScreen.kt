package com.alonso.explorersaga.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.alonso.explorersaga.R
import com.alonso.explorersaga.model.Place
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

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

                HorizontalDivider()

                // Mapa
                PlaceMap(
                    latitude = place.latitude,
                    longitude = place.longitude,
                    placeName = place.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                // Botón para visitar el sitio web
                place.website?.let { url ->
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Text("Visitar Sitio Web", modifier = Modifier.padding(start = 8.dp))
                    }
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

@Composable
private fun PlaceMap(
    latitude: Double,
    longitude: Double,
    placeName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Remember the MapView instance
    val mapView = remember {
        MapView(context)
    }

    // Lifecycle handling for the MapView
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = lifecycle, key2 = mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            mapView.onDetach()
        }
    }

    // Use AndroidView to embed the MapView
    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            // This block is called on recomposition
            view.setTileSource(TileSourceFactory.MAPNIK)
            view.setMultiTouchControls(true)

            val geoPoint = GeoPoint(latitude, longitude)
            view.controller.setZoom(18.0)
            view.controller.setCenter(geoPoint)

            // Clear previous overlays and add the new one
            view.overlays.clear()
            val marker = Marker(view)
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = placeName
            view.overlays.add(marker)
            view.invalidate() // Force a redraw
        }
    )
}
