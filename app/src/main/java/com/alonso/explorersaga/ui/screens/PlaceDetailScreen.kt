package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.ui.composables.BackTopBar
import com.alonso.explorersaga.ui.composables.CategoryIconBox
import com.alonso.explorersaga.ui.composables.dial
import com.alonso.explorersaga.ui.composables.directions
import com.alonso.explorersaga.ui.composables.isEnglish
import com.alonso.explorersaga.ui.composables.openUrl
import com.alonso.explorersaga.ui.composables.shareText
import com.alonso.explorersaga.ui.model.categoryInfo
import com.alonso.explorersaga.ui.viewmodels.Loadable
import com.alonso.explorersaga.ui.viewmodels.PlaceDetailViewModel

@Composable
fun PlaceDetailScreen(
    viewModel: PlaceDetailViewModel,
    onBack: () -> Unit,
    onShowOnMap: (String) -> Unit
) {
    val state by viewModel.place.collectAsStateWithLifecycle()
    val favorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val english = isEnglish()
    val place = (state as? Loadable.Ready)?.value

    Scaffold(
        topBar = {
            // El nombre ya está como titular de la ficha: la barra solo lleva acciones
            BackTopBar(title = "", onBack = onBack) {
                if (place != null) {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(
                                if (favorite) R.string.cd_remove_favorite else R.string.cd_add_favorite
                            ),
                            tint = if (favorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    val shareText = stringResource(
                        R.string.detail_share_text, place.displayName(english), place.municipality,
                        "https://www.openstreetmap.org/?mlat=${place.latitude}&mlon=${place.longitude}#map=17/${place.latitude}/${place.longitude}"
                    )
                    IconButton(onClick = { context.shareText(shareText) }) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.detail_share))
                    }
                }
            }
        }
    ) { padding ->
        when {
            state == Loadable.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            place == null -> Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.detail_missing), style = MaterialTheme.typography.bodyLarge)
            }
            else -> PlaceContent(
                place = place,
                english = english,
                modifier = Modifier.padding(padding),
                onShowOnMap = { onShowOnMap(place.id) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlaceContent(place: Place, english: Boolean, modifier: Modifier, onShowOnMap: () -> Unit) {
    val context = LocalContext.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (place.imageUrl != null) {
            Box(Modifier.fillMaxWidth().height(260.dp)) {
                CategoryIconBox(place)
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = place.displayName(english),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            ImageCredit(place)
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                place.displayName(english),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(categoryInfo(place.category).label)) },
                    leadingIcon = { Icon(categoryInfo(place.category).icon, null, Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(place.municipality + provinceSuffix(place.province)) },
                    leadingIcon = { Icon(Icons.Filled.Place, null, Modifier.size(18.dp)) }
                )
                if (place.heritage) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.badge_heritage)) },
                        leadingIcon = { Icon(Icons.Filled.Verified, null, Modifier.size(18.dp)) }
                    )
                }
                place.official?.let {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.badge_official, it)) },
                        leadingIcon = { Icon(Icons.Filled.Verified, null, Modifier.size(18.dp)) }
                    )
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(Icons.Filled.Directions, R.string.detail_directions) {
                    context.directions(place.latitude, place.longitude, place.name)
                }
                ActionButton(Icons.Filled.Map, R.string.detail_show_on_map, onShowOnMap)
                place.phone?.let { phone -> ActionButton(Icons.Filled.Call, R.string.detail_call) { context.dial(phone) } }
                place.website?.let { url -> ActionButton(Icons.Filled.Language, R.string.detail_website) { context.openUrl(url) } }
            }

            val (description, otherLanguage) = place.description(english)
            if (description == null) {
                HorizontalDivider()
                Text(
                    stringResource(
                        R.string.detail_generic_description,
                        stringResource(categoryInfo(place.category).label),
                        place.municipality,
                        provinceName(place.province)
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                HorizontalDivider()
                Text(description, style = MaterialTheme.typography.bodyLarge)
                if (otherLanguage) {
                    Text(
                        stringResource(if (english) R.string.detail_spanish_only else R.string.detail_english_only),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                place.wikipediaUrl(english)?.let { url ->
                    TextButton(onClick = { context.openUrl(url) }) {
                        Text(stringResource(R.string.detail_read_more_wikipedia))
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.padding(start = 6.dp).size(16.dp))
                    }
                }
            }

            if (place.address != null || place.openingHours != null || place.wheelchair != null) {
                HorizontalDivider()
                place.address?.let { InfoRow(Icons.Filled.Place, R.string.detail_address, it) }
                place.openingHours?.let { InfoRow(Icons.Filled.Schedule, R.string.detail_hours, it) }
                place.wheelchair?.let { InfoRow(Icons.AutoMirrored.Filled.Accessible, R.string.detail_wheelchair, wheelchairText(it)) }
            }

            HorizontalDivider()
            Text(
                stringResource(R.string.detail_sources, sourcesText(place.sources)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.detail_report_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImageCredit(place: Place) {
    val context = LocalContext.current
    val credit = listOfNotNull(place.imageAuthor, place.imageLicense).joinToString(" · ")
    Text(
        stringResource(R.string.detail_photo_credit, credit),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = place.imagePage != null, role = Role.Button) {
                place.imagePage?.let(context::openUrl)
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ActionButton(icon: ImageVector, label: Int, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(stringResource(label), Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: Int, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.padding(start = 12.dp)) {
            Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun provinceSuffix(province: String?): String = when (province) {
    "BA" -> " (" + stringResource(R.string.province_badajoz) + ")"
    "CC" -> " (" + stringResource(R.string.province_caceres) + ")"
    else -> ""
}

@Composable
private fun provinceName(province: String?): String = when (province) {
    "BA" -> stringResource(R.string.province_badajoz)
    "CC" -> stringResource(R.string.province_caceres)
    else -> "Extremadura"
}

@Composable
private fun wheelchairText(value: String): String = stringResource(
    when (value) {
        "yes" -> R.string.wheelchair_yes
        "limited" -> R.string.wheelchair_limited
        "no" -> R.string.wheelchair_no
        else -> R.string.wheelchair_unknown
    }
)

private fun sourcesText(sources: String): String = sources.split(',').filter { it.isNotBlank() }.joinToString(", ") {
    when (it) {
        "osm" -> "OpenStreetMap"
        "wikidata" -> "Wikidata"
        "wikipedia" -> "Wikipedia"
        "commons" -> "Wikimedia Commons"
        "junta" -> "Junta de Extremadura"
        else -> it
    }
}
