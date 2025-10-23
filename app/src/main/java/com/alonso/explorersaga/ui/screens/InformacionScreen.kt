package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alonso.explorersaga.R

@Composable
fun InformacionScreen(navController: NavController) {
    val verdeBandera = Color(0xFF007A33)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Encabezado con imagen y texto superpuesto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.teatro_merida),
                    contentDescription = "Teatro Romano de Mérida",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Overlay oscuro para mejorar la legibilidad del texto
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = 200f
                            )
                        )
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 16.dp).align(Alignment.BottomCenter)) {
                    Text(
                        text = "Descubre Mérida",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Historia viva de Extremadura",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            // Cuerpo con la descripción
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Mérida, la antigua Emerita Augusta, fue fundada en el 25 a.C. y es hoy uno de los conjuntos arqueológicos más importantes de España. Pasea por sus calles y descubre su impresionante legado romano.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pie con los botones de navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { navController.navigate("mapa") },
                    colors = ButtonDefaults.buttonColors(containerColor = verdeBandera),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(text = "🗺️ Mapa", color = Color.White, fontSize = 16.sp)
                }
                Button(
                    onClick = { navController.navigate("lugares") },
                    colors = ButtonDefaults.buttonColors(containerColor = verdeBandera),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(text = "📍 Lugares", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}
