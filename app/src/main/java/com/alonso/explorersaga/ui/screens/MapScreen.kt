package com.alonso.explorersaga.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.LocationProvider
import com.alonso.explorersaga.data.OfflineMapManager
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.ui.composables.MapLibreView
import com.alonso.explorersaga.ui.composables.PlaceThumbnail
import com.alonso.explorersaga.ui.composables.directions
import com.alonso.explorersaga.ui.composables.isEnglish
import com.alonso.explorersaga.ui.composables.mapStyleUrl
import com.alonso.explorersaga.ui.model.PlaceGroup
import com.alonso.explorersaga.ui.model.categoryInfo
import com.alonso.explorersaga.ui.viewmodels.ExploreViewModel
import com.alonso.explorersaga.ui.viewmodels.PlaceItem
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val SOURCE = "places"
private const val CLUSTERS = "clusters"
private const val CLUSTER_COUNT = "cluster-count"
private const val POINTS = "points"
private const val SELECTED = "selected"

@Composable
fun MapScreen(viewModel: ExploreViewModel, darkTheme: Boolean, onOpenPlace: (String) -> Unit) {
    val context = LocalContext.current
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val focused by viewModel.focusedPlace.collectAsStateWithLifecycle()
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }
    var selected by remember { mutableStateOf<Place?>(null) }
    val itemsById = remember(results) { results.items.associateBy { it.place.id } }
    // El listener del mapa se registra una vez: así lee siempre los últimos resultados
    val currentItems by rememberUpdatedState(itemsById)
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.refreshLocation()
        map?.let { m -> style?.let { s -> enableLocation(context, m, s) } }
        centerOnUser(viewModel, map)
    }

    Box(Modifier.fillMaxSize()) {
        MapLibreView(mapStyleUrl(darkTheme), Modifier.fillMaxSize()) { loadedMap, loadedStyle ->
            addPlaceLayers(loadedStyle, darkTheme)
            enableLocation(context, loadedMap, loadedStyle)
            if (map == null) {
                val camera = viewModel.mapCamera
                if (camera != null) {
                    loadedMap.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(camera.first, camera.second)).zoom(camera.third).build()
                } else {
                    loadedMap.moveCamera(CameraUpdateFactory.newLatLngBounds(OfflineMapManager.EXTREMADURA_BOUNDS, 48))
                }
                loadedMap.addOnCameraIdleListener {
                    val position = loadedMap.cameraPosition
                    position.target?.let { viewModel.mapCamera = Triple(it.latitude, it.longitude, position.zoom) }
                }
                loadedMap.addOnMapClickListener { point ->
                    val screen = loadedMap.projection.toScreenLocation(point)
                    val area = RectF(screen.x - 24, screen.y - 24, screen.x + 24, screen.y + 24)
                    val cluster = loadedMap.queryRenderedFeatures(area, CLUSTERS).firstOrNull()
                    if (cluster != null) {
                        loadedMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, loadedMap.cameraPosition.zoom + 2))
                        return@addOnMapClickListener true
                    }
                    val id = loadedMap.queryRenderedFeatures(area, POINTS).firstOrNull()?.getStringProperty("id")
                    selected = id?.let { currentItems[it]?.place }
                    true
                }
            }
            map = loadedMap
            style = loadedStyle
        }

        GroupChips(
            selected = filters.group,
            onSelect = viewModel::setGroup,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        SmallFloatingActionButton(
            onClick = {
                if (LocationProvider(context).hasPermission()) centerOnUser(viewModel, map)
                else permission.launch(LocationProvider.PERMISSIONS)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selected != null) 180.dp else 16.dp)
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.cd_my_location))
        }

        selected?.let { place ->
            SelectedPlaceCard(
                place = place,
                onOpen = { onOpenPlace(place.id) },
                onClose = { selected = null },
                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)
            )
        }
    }

    // Datos del mapa: se actualizan con cada cambio de filtros o búsqueda
    LaunchedEffect(style, results) {
        style?.getSourceAs<GeoJsonSource>(SOURCE)?.setGeoJson(toFeatures(results.items))
    }
    LaunchedEffect(style, selected) {
        (style?.getLayer(SELECTED) as? CircleLayer)?.setFilter(
            Expression.eq(Expression.get("id"), Expression.literal(selected?.id ?: ""))
        )
    }
    // Lugar pedido desde una ficha: se centra y se selecciona aunque no pase los filtros
    LaunchedEffect(map, focused) {
        val place = focused ?: return@LaunchedEffect
        val target = map ?: return@LaunchedEffect
        target.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(place.latitude, place.longitude), 15.0))
        selected = place
        viewModel.focusOnMap(null)
    }
}

@Composable
private fun SelectedPlaceCard(place: Place, onOpen: () -> Unit, onClose: () -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val english = isEnglish()
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlaceThumbnail(place, size = 64.dp)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        place.displayName(english),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${stringResource(categoryInfo(place.category).label)} · ${place.municipality}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { context.directions(place.latitude, place.longitude, place.name) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Directions, null)
                    Text(stringResource(R.string.detail_directions), Modifier.padding(start = 6.dp), maxLines = 1)
                }
                Button(onClick = onOpen, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.map_open_detail), maxLines = 1)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

private fun toFeatures(items: List<PlaceItem>): FeatureCollection =
    FeatureCollection.fromFeatures(items.map { item ->
        Feature.fromGeometry(Point.fromLngLat(item.place.longitude, item.place.latitude)).apply {
            addStringProperty("id", item.place.id)
            addStringProperty("group", item.place.groupKey)
        }
    })

/** Capas de lugares agrupados (clusters) y sueltos, coloreados por grupo. */
private fun addPlaceLayers(style: Style, darkTheme: Boolean) {
    if (style.getSource(SOURCE) != null) return
    style.addSource(
        GeoJsonSource(
            SOURCE,
            FeatureCollection.fromFeatures(emptyList()),
            GeoJsonOptions().withCluster(true).withClusterMaxZoom(13).withClusterRadius(45)
        )
    )
    val stroke = if (darkTheme) android.graphics.Color.BLACK else android.graphics.Color.WHITE
    val clusterColor = PlaceGroup.NATURE.color.toArgb()
    style.addLayer(
        CircleLayer(CLUSTERS, SOURCE)
            .withFilter(Expression.has("point_count"))
            .withProperties(
                PropertyFactory.circleColor(clusterColor),
                PropertyFactory.circleRadius(
                    Expression.step(
                        Expression.get("point_count"), Expression.literal(16f),
                        Expression.stop(25, 20f), Expression.stop(100, 26f), Expression.stop(500, 32f)
                    )
                ),
                PropertyFactory.circleStrokeWidth(2f),
                PropertyFactory.circleStrokeColor(stroke)
            )
    )
    style.addLayer(
        SymbolLayer(CLUSTER_COUNT, SOURCE)
            .withFilter(Expression.has("point_count"))
            .withProperties(
                PropertyFactory.textField(Expression.toString(Expression.get("point_count_abbreviated"))),
                PropertyFactory.textFont(arrayOf("Noto Sans Regular")),
                PropertyFactory.textSize(13f),
                PropertyFactory.textColor(android.graphics.Color.WHITE),
                PropertyFactory.textAllowOverlap(true),
                PropertyFactory.textIgnorePlacement(true)
            )
    )
    val groupColor = Expression.match(
        Expression.get("group"),
        Expression.color(PlaceGroup.SERVICES.color.toArgb()),
        *PlaceGroup.entries.map { Expression.stop(it.name, Expression.color(it.color.toArgb())) }.toTypedArray()
    )
    style.addLayer(
        CircleLayer(SELECTED, SOURCE)
            .withFilter(Expression.eq(Expression.get("id"), Expression.literal("")))
            .withProperties(
                PropertyFactory.circleRadius(14f),
                PropertyFactory.circleColor(groupColor),
                PropertyFactory.circleOpacity(0.35f)
            )
    )
    style.addLayer(
        CircleLayer(POINTS, SOURCE)
            .withFilter(Expression.not(Expression.has("point_count")))
            .withProperties(
                PropertyFactory.circleColor(groupColor),
                PropertyFactory.circleRadius(7f),
                PropertyFactory.circleStrokeWidth(2f),
                PropertyFactory.circleStrokeColor(stroke)
            )
    )
}

@SuppressLint("MissingPermission")
private fun enableLocation(context: Context, map: MapLibreMap, style: Style) {
    if (!LocationProvider(context).hasPermission()) return
    map.locationComponent.apply {
        activateLocationComponent(LocationComponentActivationOptions.builder(context, style).build())
        isLocationComponentEnabled = true
        renderMode = RenderMode.NORMAL
    }
}

@SuppressLint("MissingPermission")
private fun centerOnUser(viewModel: ExploreViewModel, map: MapLibreMap?) {
    viewModel.refreshLocation()
    val target = map ?: return
    val fromMap = runCatching { target.locationComponent.lastKnownLocation }.getOrNull()
    val latLng = fromMap?.let { LatLng(it.latitude, it.longitude) }
        ?: viewModel.location.value?.let { LatLng(it.latitude, it.longitude) }
        ?: return
    target.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0))
}
