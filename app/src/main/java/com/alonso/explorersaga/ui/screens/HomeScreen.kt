package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alonso.explorersaga.R

@Composable
fun HomeScreen(navController: NavController) {
    val verdeBandera = Color(0xFF007A33)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fondo de imagen
            Image(
                painter = painterResource(id = R.drawable.fondo_campo),
                contentDescription = "Fondo de un campo de Extremadura",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ExplorerSaga Extremadura",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp
                )
                Text(
                    text = "Tu próxima aventura empieza aquí",
                    fontSize = 18.sp,
                    color = Color.White,
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
}
