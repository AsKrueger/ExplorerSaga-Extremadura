package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.R
import com.alonso.explorersaga.ui.composables.PlaceListItem
import com.alonso.explorersaga.ui.viewmodels.TripItem
import com.alonso.explorersaga.ui.viewmodels.TripViewModel

private const val MAX_DAYS = 7

@Composable
fun TripScreen(
    viewModel: TripViewModel,
    onOpenPlace: (String) -> Unit,
    onExplore: () -> Unit,
    onOpenRoutes: () -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.trip_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp).semantics { heading() }
        )
        when (val list = items) {
            null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else -> if (list.isEmpty()) {
                EmptyTrip(onExplore, onOpenRoutes)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            stringResource(R.string.trip_intro),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    list.groupBy { it.day }.forEach { (day, dayItems) ->
                        item(key = "day-$day") {
                            Text(
                                if (day == 0) stringResource(R.string.trip_no_day) else stringResource(R.string.trip_day, day),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp).semantics { heading() }
                            )
                        }
                        items(dayItems, key = { it.place.id }) { item ->
                            TripRow(
                                item = item,
                                onOpen = { onOpenPlace(item.place.id) },
                                onSetDay = { viewModel.setDay(item.place.id, it) },
                                onRemove = { viewModel.remove(item.place.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripRow(item: TripItem, onOpen: () -> Unit, onSetDay: (Int) -> Unit, onRemove: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Column {
        PlaceListItem(item.place, distanceMeters = null, onClick = onOpen)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = stringResource(R.string.cd_set_day))
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.trip_no_day)) },
                        onClick = { menu = false; onSetDay(0) }
                    )
                    (1..MAX_DAYS).forEach { day ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.trip_day, day)) },
                            onClick = { menu = false; onSetDay(day) }
                        )
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_remove_favorite))
            }
        }
    }
}

@Composable
private fun EmptyTrip(onExplore: () -> Unit, onOpenRoutes: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.FavoriteBorder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.trip_empty_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
        Text(
            stringResource(R.string.trip_empty_text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        Button(onClick = onExplore, modifier = Modifier.padding(top = 16.dp)) { Text(stringResource(R.string.action_explore)) }
        OutlinedButton(onClick = onOpenRoutes, modifier = Modifier.padding(top = 8.dp)) { Text(stringResource(R.string.action_see_routes)) }
    }
}
