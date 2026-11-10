package com.alonso.explorersaga.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.alonso.explorersaga.R
import com.alonso.explorersaga.model.Place
import com.alonso.explorersaga.ui.composables.rememberPlaceImageModel
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@Composable
fun PlaceDetailScreen(
    placeId: Int,
    viewModel: PlacesViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(placeId) { viewModel.loadPlaceById(placeId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val place = uiState.selectedPlace?.takeIf { it.id == placeId }

    if (place == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        PlaceDetailContent(place = place, onBack = onBack)
    }
}

@Composable
private fun PlaceDetailContent(place: Place, onBack: () -> Unit) {
    val context = LocalContext.current
    val imageModel = rememberPlaceImageModel(place.photo)
    // Las descripciones de los lugares aún solo existen en español (se traducen en la Issue #30)
    val showSpanishOnlyNote = LocalConfiguration.current.locales[0].language != "es"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AsyncImage(
                model = imageModel ?: R.drawable.teatro_merida,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.teatro_merida),
                error = painterResource(id = R.drawable.teatro_merida)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0x66000000), Color.Transparent, Color(0xCC000000))))
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
                    .background(Color(0x66000000), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = Color.White
                )
            }
            Text(
                text = place.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { openDirections(context, place) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_directions))
                }
                OutlinedButton(onClick = { sharePlace(context, place) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_share))
                }
            }

            if (showSpanishOnlyNote) {
                Text(
                    text = stringResource(R.string.detail_spanish_only),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = place.description, style = MaterialTheme.typography.bodyLarge)

            HorizontalDivider()

            place.horarios?.takeIf { it.isNotBlank() }?.let {
                InfoRow(icon = Icons.Filled.Schedule, label = stringResource(R.string.detail_hours), value = it)
            }
            place.direccion?.takeIf { it.isNotBlank() }?.let {
                InfoRow(icon = Icons.Filled.Place, label = stringResource(R.string.detail_address), value = it)
            }
            place.website?.takeIf { it.isNotBlank() }?.let { url ->
                OutlinedButton(onClick = { openUrl(context, url) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_website))
                }
            }

            PlaceMap(
                latitude = place.latitude,
                longitude = place.longitude,
                placeName = place.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = stringResource(R.string.map_attribution),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// Abre la app de mapas del móvil con la ruta hasta el lugar (Google Maps, Organic Maps, OsmAnd…)
private fun openDirections(context: Context, place: Place) {
    val coordinates = String.format(Locale.US, "%.6f,%.6f", place.latitude, place.longitude)
    val label = android.net.Uri.encode(place.name)
    openUri(context, "geo:0,0?q=$coordinates($label)")
}

private fun sharePlace(context: Context, place: Place) {
    val mapsLink = String.format(
        Locale.US,
        "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=18/%.6f/%.6f",
        place.latitude, place.longitude, place.latitude, place.longitude
    )
    val text = context.getString(R.string.detail_share_text, place.name, mapsLink)
    val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(intent, null))
}

private fun openUrl(context: Context, url: String) = openUri(context, url)

private fun openUri(context: Context, uri: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
    } catch (_: ActivityNotFoundException) {
        // Sin app capaz de abrirlo no hay nada útil que hacer
    }
}

@Composable
private fun PlaceMap(
    latitude: Double,
    longitude: Double,
    placeName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
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

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            val geoPoint = GeoPoint(latitude, longitude)
            view.controller.setZoom(17.0)
            view.controller.setCenter(geoPoint)
            view.overlays.clear()
            view.overlays.add(
                Marker(view).apply {
                    position = geoPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = placeName
                }
            )
            view.invalidate()
        }
    )
}
