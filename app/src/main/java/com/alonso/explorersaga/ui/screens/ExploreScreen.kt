package com.alonso.explorersaga.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.LocationProvider
import com.alonso.explorersaga.data.db.Municipality
import com.alonso.explorersaga.ui.composables.PlaceListItem
import com.alonso.explorersaga.ui.model.PlaceGroup
import com.alonso.explorersaga.ui.viewmodels.ExploreViewModel
import com.alonso.explorersaga.util.foldForSearch

@Composable
fun ExploreScreen(viewModel: ExploreViewModel, onOpenPlace: (String) -> Unit) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val municipalities by viewModel.municipalities.collectAsStateWithLifecycle()
    var choosingMunicipality by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.refreshLocation()
    }
    LaunchedEffect(Unit) { viewModel.refreshLocation() }
    // Al cambiar filtros se vuelve arriba para ver los primeros resultados
    LaunchedEffect(filters) { listState.scrollToItem(0) }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = filters.query,
            onValueChange = viewModel::setQuery,
            label = { Text(stringResource(R.string.search_label)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (filters.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuery("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.cd_clear_search))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)
        )

        GroupChips(selected = filters.group, onSelect = viewModel::setGroup)

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = filters.province == "BA",
                    onClick = { viewModel.setProvince(if (filters.province == "BA") null else "BA") },
                    label = { Text(stringResource(R.string.province_badajoz)) }
                )
            }
            item {
                FilterChip(
                    selected = filters.province == "CC",
                    onClick = { viewModel.setProvince(if (filters.province == "CC") null else "CC") },
                    label = { Text(stringResource(R.string.province_caceres)) }
                )
            }
            item {
                FilterChip(
                    selected = filters.municipality != null,
                    onClick = {
                        if (filters.municipality != null) viewModel.setMunicipality(null) else choosingMunicipality = true
                    },
                    label = { Text(filters.municipality ?: stringResource(R.string.filter_municipality)) },
                    leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                    trailingIcon = if (filters.municipality != null) {
                        { Icon(Icons.Filled.Clear, stringResource(R.string.cd_clear_municipality)) }
                    } else null
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                pluralStringResource(R.plurals.results_count, results.items.size, results.items.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (location == null) {
                TextButton(onClick = { permission.launch(LocationProvider.PERMISSIONS) }) {
                    Icon(Icons.Filled.MyLocation, null)
                    Text(stringResource(R.string.action_sort_nearby), Modifier.padding(start = 6.dp))
                }
            } else {
                Text(
                    stringResource(R.string.sorted_by_distance),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.heightIn(min = 48.dp).padding(vertical = 14.dp)
                )
            }
        }

        when {
            results.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            results.items.isEmpty() -> EmptyResults(
                canClear = !filters.isDefault,
                onClear = viewModel::clearFilters
            )
            else -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results.items, key = { it.place.id }) { item ->
                    PlaceListItem(item.place, item.distanceMeters, onClick = { onOpenPlace(item.place.id) })
                }
            }
        }
    }

    if (choosingMunicipality) {
        MunicipalityDialog(
            municipalities = municipalities.filter { filters.province == null || it.province == filters.province },
            onSelect = {
                viewModel.setMunicipality(it)
                choosingMunicipality = false
            },
            onDismiss = { choosingMunicipality = false }
        )
    }
}

@Composable
fun GroupChips(selected: PlaceGroup?, onSelect: (PlaceGroup?) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.group_all)) }
            )
        }
        items(PlaceGroup.entries) { group ->
            FilterChip(
                selected = selected == group,
                onClick = { onSelect(if (selected == group) null else group) },
                label = { Text(stringResource(group.label)) },
                leadingIcon = { Icon(group.icon, contentDescription = null, tint = group.color) }
            )
        }
    }
}

@Composable
private fun EmptyResults(canClear: Boolean, onClear: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
        Text(stringResource(R.string.empty_text), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        if (canClear) {
            OutlinedButton(onClick = onClear, modifier = Modifier.padding(top = 16.dp)) {
                Text(stringResource(R.string.action_clear_filters))
            }
        }
    }
}

@Composable
private fun MunicipalityDialog(
    municipalities: List<Municipality>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, municipalities) {
        val folded = query.foldForSearch()
        municipalities.filter { folded.isEmpty() || it.name.foldForSearch().contains(folded) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_municipality)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.search_municipality)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(Modifier.heightIn(max = 360.dp).padding(top = 8.dp)) {
                    items(filtered, key = { it.name }) { municipality ->
                        Text(
                            municipality.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(municipality.name) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
