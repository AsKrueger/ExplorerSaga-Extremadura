package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        ) {
            Text(
                text = "Filtrar lugares",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            FilterOption(
                text = "Monumentos",
                checked = filterState.monuments
            ) { isChecked ->
                viewModel.updateFilters(filterState.copy(monuments = isChecked))
            }
            FilterOption(
                text = "Restaurantes",
                checked = filterState.restaurants
            ) { isChecked ->
                viewModel.updateFilters(filterState.copy(restaurants = isChecked))
            }
            FilterOption(
                text = "Tiendas",
                checked = filterState.shops
            ) { isChecked ->
                viewModel.updateFilters(filterState.copy(shops = isChecked))
            }

            Spacer(modifier = Modifier.height(24.dp))

            FilterOption(
                text = "Más populares primero",
                checked = filterState.popularFirst
            ) { isChecked ->
                viewModel.updateFilters(filterState.copy(popularFirst = isChecked))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Aplicar filtros",
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = text, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
    }
}
