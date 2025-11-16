package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonso.explorersaga.R

@Composable
fun PlacesScreen(
    onCategorySelected: (String) -> Unit,
    onMapClicked: () -> Unit,
    onInfoClicked: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.places_title),
                fontFamily = playfairDisplayFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Enviamos una categoría especial "historico" para el caso combinado
            CategoryCard(text = stringResource(id = R.string.places_monuments), onClick = { onCategorySelected("historico") })
            Spacer(modifier = Modifier.height(16.dp))
            CategoryCard(text = stringResource(id = R.string.places_restaurants), onClick = { onCategorySelected("gastronomia") })
            Spacer(modifier = Modifier.height(16.dp))
            CategoryCard(text = stringResource(id = R.string.places_shops), onClick = { onCategorySelected("tienda") })
            
            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onMapClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(text = "🗺️ Mapa", fontFamily = montserratFamily, color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)
                }
                Button(
                    onClick = onInfoClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(text = "🏛️ Info", fontFamily = montserratFamily, color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)
                }
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontFamily = montserratFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
