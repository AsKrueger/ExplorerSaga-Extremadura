package com.alonso.explorersaga.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.LocationProvider
import com.alonso.explorersaga.ui.composables.BackTopBar
import com.alonso.explorersaga.ui.composables.LocationCard
import com.alonso.explorersaga.ui.composables.PlaceListItem
import com.alonso.explorersaga.ui.composables.dial
import com.alonso.explorersaga.ui.model.categoryInfo
import com.alonso.explorersaga.ui.viewmodels.HomeViewModel
import com.alonso.explorersaga.ui.viewmodels.ServicesViewModel

@Composable
fun AgendaScreen(viewModel: HomeViewModel, onBack: () -> Unit) {
    val agenda by viewModel.agenda.collectAsStateWithLifecycle()
    Scaffold(topBar = { BackTopBar(stringResource(R.string.agenda_title), onBack) }) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.agenda_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(agenda, key = { it.event.id }) { EventCard(it, Modifier.fillMaxWidth()) }
        }
    }
}

@Composable
fun ServicesScreen(viewModel: ServicesViewModel, onBack: () -> Unit, onOpenPlace: (String) -> Unit) {
    val context = LocalContext.current
    val location by viewModel.location.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.refreshLocation()
    }
    LaunchedEffect(Unit) { viewModel.refreshLocation() }

    Scaffold(topBar = { BackTopBar(stringResource(R.string.services_title), onBack) }) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Emergency, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(stringResource(R.string.services_emergency), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.services_emergency_text), style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { context.dial("112") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Filled.Call, null)
                            Text("112", Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
            if (location == null) {
                item {
                    LocationCard(
                        text = stringResource(R.string.services_permission),
                        onRequest = { permission.launch(LocationProvider.PERMISSIONS) }
                    )
                }
            }
            services.forEach { group ->
                item(key = "title-" + group.category) {
                    Text(
                        stringResource(categoryInfo(group.category).label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp).semantics { heading() }
                    )
                }
                if (group.nearest.isEmpty()) {
                    item(key = "empty-" + group.category) {
                        Text(stringResource(R.string.services_none), style = MaterialTheme.typography.bodySmall)
                    }
                }
                items(group.nearest, key = { it.place.id }) {
                    PlaceListItem(it.place, it.distanceMeters, { onOpenPlace(it.place.id) })
                }
            }
            item {
                Text(
                    stringResource(R.string.services_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }
        }
    }
}
