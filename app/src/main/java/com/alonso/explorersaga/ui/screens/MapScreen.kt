package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(navController: NavController) {
    val verdeBandera = Color(0xFF007A33)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Usamos AndroidView para incrustar la vista de mapa clásica de osmdroid
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    // El factory se usa solo para crear e inicializar la vista
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(38.915, -6.345)) // Coordenadas de Mérida
                    }
                },
                update = { mapView ->
                    // El update se usa para modificar la vista, como añadir marcadores
                    mapView.overlays.clear() // Limpia marcadores anteriores

                    val teatroRomano = GeoPoint(38.9157, -6.3386)
                    val marker = Marker(mapView)
                    marker.position = teatroRomano
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Teatro Romano"
                    mapView.overlays.add(marker)

                    mapView.invalidate() // Refresca el mapa
                }
            )

            // Botones flotantes (FABs)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("filtrado") },
                    containerColor = verdeBandera
                ) {
                    Text(text = "🔍", fontSize = 24.sp, color = Color.White)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                 FloatingActionButton(
                    onClick = { /* TODO: Navegar a Rutas */ },
                    containerColor = verdeBandera
                ) {
                    Text(text = "🧭", fontSize = 24.sp, color = Color.White)
                }
            }
             Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                 FloatingActionButton(
                    onClick = { /* TODO: Centrar en mi ubicación */ },
                    containerColor = Color.White
                ) {
                    Text(text = "📍", fontSize = 24.sp, color = verdeBandera)
                }
            }
        }
    }
}
