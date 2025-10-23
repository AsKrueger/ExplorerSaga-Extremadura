package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    // Definimos los colores de la paleta para esta pantalla
    val verdeBandera = Color(0xFF007A33)
    val negroSuave = Color(0xFF222222)

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ExplorerSaga Extremadura",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = negroSuave,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
            Text(
                text = "Tu próxima aventura empieza aquí",
                fontSize = 18.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
            Button(
                onClick = { navController.navigate("informacion") },
                colors = ButtonDefaults.buttonColors(containerColor = verdeBandera),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "Explorar Mérida",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        }
    }
}
