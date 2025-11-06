package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alonso.explorersaga.navigation.AppScreens
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(navController: NavController, viewModel: PlacesViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(38.915, -6.345))
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    uiState.places.forEach { place ->
                        val geoPoint = GeoPoint(place.latitude, place.longitude)
                        val marker = Marker(mapView)
                        marker.position = geoPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = place.name
                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate(AppScreens.Filtrado.route) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(text = "🔍", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                 FloatingActionButton(
                    onClick = { /* TODO: Navegar a Rutas */ },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(text = "🧭", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
             Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                 FloatingActionButton(
                    onClick = { /* TODO: Centrar en mi ubicación */ },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Text(text = "📍", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
