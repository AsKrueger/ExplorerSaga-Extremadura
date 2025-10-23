package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FilterScreen(navController: NavController) {
    val verdeBandera = Color(0xFF007A33)

    val monumentosState = remember { mutableStateOf(true) }
    val restaurantesState = remember { mutableStateOf(true) }
    val tiendasState = remember { mutableStateOf(true) }
    val popularesState = remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Filtrar lugares",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = verdeBandera,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            FilterOption(text = "Monumentos", checked = monumentosState.value) { monumentosState.value = it }
            FilterOption(text = "Restaurantes", checked = restaurantesState.value) { restaurantesState.value = it }
            FilterOption(text = "Tiendas", checked = tiendasState.value) { tiendasState.value = it }

            Spacer(modifier = Modifier.height(24.dp))

            FilterOption(text = "Más populares primero", checked = popularesState.value) { popularesState.value = it }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.popBackStack() }, // Vuelve al mapa
                colors = ButtonDefaults.buttonColors(containerColor = verdeBandera),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Aplicar filtros",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FilterOption(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = text, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
    }
}
