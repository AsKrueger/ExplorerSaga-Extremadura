package com.alonso.explorersaga.ui.screens

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.R
import com.alonso.explorersaga.model.Place
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    viewModel: PlacesViewModel,
    onFilterClicked: () -> Unit,
    onRoutesClicked: () -> Unit, // TODO
    onCenterLocationClicked: () -> Unit, // TODO
    onInfoClicked: () -> Unit,
    onPlacesClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val mapView = rememberMapViewWithLifecycle()

            AndroidView({ mapView })

            LaunchedEffect(uiState.places) {
                mapView.updateMarkers(uiState.places)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = onFilterClicked,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = stringResource(id = R.string.map_filter_icon), 
                        fontFamily = montserratFamily,
                        fontSize = 24.sp, 
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onInfoClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(
                        text = "🏛️ Info", 
                        fontFamily = montserratFamily,
                        color = MaterialTheme.colorScheme.onPrimary, 
                        fontSize = 16.sp
                    )
                }
                Button(
                    onClick = onPlacesClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.info_places_button), 
                        fontFamily = montserratFamily,
                        color = MaterialTheme.colorScheme.onPrimary, 
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

private fun MapView.updateMarkers(places: List<Place>) {
    val context = this.context
    this.overlays.clear()

    val colorHistorico = Color(0xFF556B2F).toArgb() // Verde Oliva Oscuro
    val colorGastronomia = Color(0xFFFFC107).toArgb() // Amarillo Ámbar
    val colorTiendas = Color(0xFF1976D2).toArgb() // Azul

    places.forEach { place ->
        val geoPoint = GeoPoint(place.latitude, place.longitude)
        val marker = Marker(this)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = place.name

        val iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_map_pin)?.mutate()

        val color = when (place.category) {
            "monumento", "iglesia", "museo" -> colorHistorico
            "restaurante", "cafeteria" -> colorGastronomia
            "tienda_general", "supermercado", "souvenir" -> colorTiendas
            else -> Color.Gray.toArgb()
        }

        iconDrawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        marker.icon = iconDrawable

        this.overlays.add(marker)
    }
    this.invalidate()
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0) // <-- CAMBIO APLICADO AQUÍ
            controller.setCenter(GeoPoint(38.915, -6.345))
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    return mapView
}
