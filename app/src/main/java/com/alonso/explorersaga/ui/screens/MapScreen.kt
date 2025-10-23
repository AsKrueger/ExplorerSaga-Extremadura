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
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(navController: NavController) {
    val verdeBandera = Color(0xFF007A33)
    
    // Coordenadas de Mérida y del Teatro Romano
    val merida = LatLng(38.915, -6.345)
    val teatroRomano = LatLng(38.9157, -6.3386)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(merida, 14f)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mapa de Google
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Marcador de ejemplo en el Teatro Romano
                Marker(
                    state = MarkerState(position = teatroRomano),
                    title = "Teatro Romano",
                    snippet = "Espectacular teatro del siglo I a.C."
                )
            }

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
