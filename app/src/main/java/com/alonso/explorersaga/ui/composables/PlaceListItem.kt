package com.alonso.explorersaga.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alonso.explorersaga.model.Place // <<<--- ¡ESTA ES LA LÍNEA QUE FALTABA!

@Composable
fun PlaceListItem(place: Place, onItemClicked: (Place) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClicked(place) } // Usamos la nueva lambda
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = place.name)
            // Si quieres mostrar más campos, añádelos aquí:
            // Text(text = place.description)
        }
    }
}
