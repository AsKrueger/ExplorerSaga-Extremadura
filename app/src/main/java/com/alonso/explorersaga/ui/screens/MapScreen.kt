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

@Composable
fun MapScreen(navController: NavController) {
    val verdeBandera = Color(0xFF007A33)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Placeholder para el mapa de Google
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "(Aquí irá el mapa interactivo)", fontSize = 18.sp, color = Color.Gray)
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
