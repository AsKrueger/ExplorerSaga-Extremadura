package com.alonso.explorersaga.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.DataSourceOrigin
import com.alonso.explorersaga.model.Place
import com.alonso.explorersaga.ui.composables.FilterGroup
import com.alonso.explorersaga.ui.composables.FilterOption
import com.alonso.explorersaga.ui.composables.PlaceListItem
import com.alonso.explorersaga.ui.composables.onlyOption
import com.alonso.explorersaga.ui.composables.singleGroup
import com.alonso.explorersaga.ui.viewmodels.FilterState
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun ExploreScreen(
    viewModel: PlacesViewModel,
    onPlaceClicked: (Place) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    // Los filtros se leen directamente: uiState solo se actualiza al terminar la carga de los lugares
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

    // Permiso de ubicación en tiempo de ejecución: solo se usa en el móvil para ordenar por cercanía
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) viewModel.fetchDeviceLocation(context)
    }
    LaunchedEffect(Unit) {
        val hasLocationPermission = LOCATION_PERMISSIONS.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (hasLocationPermission) {
            viewModel.fetchDeviceLocation(context)
        } else {
            locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.explore_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            label = { Text(stringResource(R.string.search_label)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.cd_clear_search))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        GroupChips(filterState = filterState, onChange = viewModel::updateFilters)
        filterState.singleGroup()?.let { group ->
            OptionChips(group = group, filterState = filterState, onChange = viewModel::updateFilters)
        }

        if (uiState.dataSourceOrigin == DataSourceOrigin.LOCAL_FALLBACK) {
            OfflineNotice(onRetry = viewModel::retry)
        }

        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (uiState.places.isEmpty() && !uiState.isLoading) {
            EmptyResults(
                onClearFilters = {
                    viewModel.onSearchQueryChanged("")
                    viewModel.updateFilters(FilterState.ALL)
                },
                onRetry = viewModel::retry
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.places, key = { it.id }) { place ->
                    PlaceListItem(
                        place = place,
                        userLocation = uiState.userLocation,
                        onItemClicked = onPlaceClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupChips(filterState: FilterState, onChange: (FilterState) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = filterState == FilterState.ALL,
                onClick = { onChange(FilterState.ALL) },
                label = { Text(stringResource(R.string.group_all)) }
            )
        }
        items(FilterGroup.entries) { group ->
            FilterChip(
                selected = filterState.singleGroup() == group,
                onClick = { onChange(group.preset) },
                label = { Text(stringResource(group.label)) }
            )
        }
    }
}

@Composable
private fun OptionChips(group: FilterGroup, filterState: FilterState, onChange: (FilterState) -> Unit) {
    val options = FilterOption.of(group)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = options.all { it.isOn(filterState) },
                onClick = { onChange(onlyOption(group, null)) },
                label = { Text(stringResource(R.string.filter_all)) }
            )
        }
        items(options) { option ->
            FilterChip(
                selected = options.filter { it.isOn(filterState) } == listOf(option),
                onClick = { onChange(onlyOption(group, option)) },
                label = { Text(stringResource(option.label)) }
            )
        }
    }
}

@Composable
private fun OfflineNotice(onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.offline_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

@Composable
private fun EmptyResults(onClearFilters: () -> Unit, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.empty_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onClearFilters) { Text(stringResource(R.string.action_clear_filters)) }
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
        }
    }
}
