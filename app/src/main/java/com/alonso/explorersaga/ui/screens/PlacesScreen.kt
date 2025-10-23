package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun PlacesScreen(navController: NavController) {
    val verdeBandera = Color(0xFF007A33)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Explora por categoría",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = verdeBandera,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            CategoryCard(text = "🏛️ Monumentos históricos") {
                navController.navigate("monuments_list")
            }
            Spacer(modifier = Modifier.height(16.dp))
            CategoryCard(text = "☕ Restaurantes y cafeterías") {
                navController.navigate("restaurants_list")
            }
            Spacer(modifier = Modifier.height(16.dp))
            CategoryCard(text = "🛍️ Tiendas y souvenirs") {
                navController.navigate("shops_list")
            }
        }
    }
}

@Composable
fun CategoryCard(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
