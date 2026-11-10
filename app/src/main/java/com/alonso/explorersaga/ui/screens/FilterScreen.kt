package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.R
import com.alonso.explorersaga.ui.composables.FilterGroup
import com.alonso.explorersaga.ui.composables.FilterOption
import com.alonso.explorersaga.ui.viewmodels.FilterState
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

@Composable
fun FilterScreen(
    viewModel: PlacesViewModel,
    onApply: () -> Unit
) {
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.filters_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { viewModel.updateFilters(FilterState.ALL) }) {
                Text(stringResource(R.string.filters_select_all))
            }
        }

        FilterGroup.entries.forEachIndexed { index, group ->
            if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text(
                text = stringResource(group.label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            FilterOption.of(group).forEach { option ->
                FilterRow(
                    text = stringResource(option.label),
                    checked = option.isOn(filterState),
                    onCheckedChange = { viewModel.updateFilters(option.set(filterState, it)) }
                )
            }
        }

        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(text = stringResource(R.string.filters_apply), modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun FilterRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    // Toda la fila es pulsable, no solo la casilla
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = 4.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
    }
}
