package com.alonso.explorersaga.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.LocationProvider
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.data.db.RouteEntity
import com.alonso.explorersaga.ui.composables.CategoryIconBox
import com.alonso.explorersaga.ui.composables.LocationCard
import com.alonso.explorersaga.ui.composables.PlaceListItem
import com.alonso.explorersaga.ui.composables.isEnglish
import com.alonso.explorersaga.ui.model.PlaceGroup
import com.alonso.explorersaga.ui.viewmodels.HomeViewModel
import com.alonso.explorersaga.ui.viewmodels.SeasonalEvent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onGroupSelected: (PlaceGroup?) -> Unit,
    onOpenPlace: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
    onOpenRoutes: () -> Unit,
    onOpenAgenda: () -> Unit,
    onOpenServices: () -> Unit,
    onOpenMap: () -> Unit
) {
    val season by viewModel.season.collectAsStateWithLifecycle()
    val featured by viewModel.featured.collectAsStateWithLifecycle()
    val routes by viewModel.routes.collectAsStateWithLifecycle()
    val nearby by viewModel.nearby.collectAsStateWithLifecycle()
    val count by viewModel.placeCount.collectAsStateWithLifecycle()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.refreshLocation()
    }
    LaunchedEffect(Unit) { viewModel.refreshLocation() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Hero(count, onOpenMap) }

        item {
            FlowRow(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlaceGroup.DISCOVER.forEach { group ->
                    AssistChip(
                        onClick = { onGroupSelected(group) },
                        label = { Text(stringResource(group.label)) },
                        leadingIcon = { Icon(group.icon, null, tint = group.color) }
                    )
                }
            }
        }

        if (season.isNotEmpty()) {
            item { SectionHeader(R.string.home_season, onSeeAll = onOpenAgenda) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(season, key = { it.event.id }) { EventCard(it, Modifier.width(280.dp)) }
                }
            }
        }

        item { SectionHeader(R.string.home_nearby) }
        when (val places = nearby) {
            null -> item {
                LocationCard(
                    text = stringResource(R.string.home_nearby_permission),
                    onRequest = { permission.launch(LocationProvider.PERMISSIONS) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            else -> if (places.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.home_nearby_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(places.take(5), key = { "near-" + it.place.id }) {
                    PlaceListItem(it.place, it.distanceMeters, { onOpenPlace(it.place.id) }, Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        if (routes.isNotEmpty()) {
            item { SectionHeader(R.string.home_routes, onSeeAll = onOpenRoutes) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(routes, key = { it.id }) { RouteCard(it, { onOpenRoute(it.id) }, Modifier.width(240.dp)) }
                }
            }
        }

        if (featured.isNotEmpty()) {
            item { SectionHeader(R.string.home_featured) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(featured, key = { it.id }) { FeaturedCard(it, { onOpenPlace(it.id) }) }
                }
            }
        }

        item {
            Card(
                onClick = onOpenServices,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalPharmacy, null)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(stringResource(R.string.services_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.services_teaser), style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
            }
        }
    }
}

@Composable
private fun Hero(count: Int, onOpenMap: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(260.dp)) {
        Image(
            painter = painterResource(R.drawable.hero_merida),
            contentDescription = stringResource(R.string.cd_hero),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
        )
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(
                stringResource(R.string.home_hero_title),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                if (count > 0) pluralStringResource(R.plurals.home_hero_subtitle, count, count)
                else stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            TextButton(onClick = onOpenMap, contentPadding = PaddingValues(0.dp)) {
                Text(stringResource(R.string.home_open_map), color = Color.White, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: Int, onSeeAll: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        onSeeAll?.let { TextButton(onClick = it) { Text(stringResource(R.string.action_see_all)) } }
    }
}

@Composable
fun EventCard(item: SeasonalEvent, modifier: Modifier = Modifier) {
    val english = isEnglish()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (item.active) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Event, null, Modifier.padding(end = 6.dp))
                Text(
                    if (item.active) stringResource(R.string.event_now)
                    else pluralStringResource(R.plurals.event_in_days, item.daysUntil.toInt(), item.daysUntil.toInt()),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Text(
                if (english) item.event.titleEn else item.event.titleEs,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(item.event.place, style = MaterialTheme.typography.labelMedium)
            Text(
                if (english) item.event.textEn else item.event.textEs,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RouteCard(route: RouteEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val english = isEnglish()
    Card(onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Filled.Route, null, tint = MaterialTheme.colorScheme.primary)
            Text(
                if (english) route.titleEn else route.titleEs,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                (if (english) route.durationEn else route.durationEs) + " · " +
                    stringResource(R.string.route_km, route.km.toInt()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeaturedCard(place: Place, onClick: () -> Unit) {
    val english = isEnglish()
    Card(onClick = onClick, modifier = Modifier.width(200.dp)) {
        Box(Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))) {
            CategoryIconBox(place)
            AsyncImage(
                model = place.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                place.displayName(english),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(place.municipality, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
