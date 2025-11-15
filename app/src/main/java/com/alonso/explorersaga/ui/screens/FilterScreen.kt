package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

@Composable
fun FilterScreen(
    navController: NavController,
    viewModel: PlacesViewModel
) {
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Filtros Generales",
                fontFamily = playfairDisplayFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- Sección de Lugares Históricos ---
            Text("Histórico", fontFamily = playfairDisplayFamily, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            FilterOption(text = "Monumentos", checked = filterState.monuments) { viewModel.updateFilters(filterState.copy(monuments = it)) }
            FilterOption(text = "Iglesias", checked = filterState.iglesias) { viewModel.updateFilters(filterState.copy(iglesias = it)) }
            FilterOption(text = "Museos", checked = filterState.museos) { viewModel.updateFilters(filterState.copy(museos = it)) }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Sección de Gastronomía ---
            Text("Gastronomía", fontFamily = playfairDisplayFamily, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            FilterOption(text = "Restaurantes", checked = filterState.restaurants) { viewModel.updateFilters(filterState.copy(restaurants = it)) }
            FilterOption(text = "Cafeterías", checked = filterState.cafeterias) { viewModel.updateFilters(filterState.copy(cafeterias = it)) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Sección de Tiendas ---
            Text("Tiendas", fontFamily = playfairDisplayFamily, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            FilterOption(text = "Tiendas Generales", checked = filterState.tiendasGenerales) { viewModel.updateFilters(filterState.copy(tiendasGenerales = it)) }
            FilterOption(text = "Supermercados", checked = filterState.supermercados) { viewModel.updateFilters(filterState.copy(supermercados = it)) }
            FilterOption(text = "Souvenirs", checked = filterState.souvenirs) { viewModel.updateFilters(filterState.copy(souvenirs = it)) }
            
            Spacer(modifier = Modifier.weight(1f))

            // --- Botón de Aplicar ---
            Button(
                onClick = { navController.popBackStack() }, // Vuelve a la pantalla anterior (el mapa)
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(
                    text = "Aplicar Filtros",
                    fontFamily = montserratFamily,
                    color = MaterialTheme.colorScheme.onPrimary,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = text, fontFamily = montserratFamily, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
    }
}
