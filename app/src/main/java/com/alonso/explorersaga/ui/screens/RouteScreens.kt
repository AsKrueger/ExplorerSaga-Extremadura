package com.alonso.explorersaga.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.content.RouteStopDto
import com.alonso.explorersaga.ui.composables.BackTopBar
import com.alonso.explorersaga.ui.composables.MapLibreView
import com.alonso.explorersaga.ui.composables.isEnglish
import com.alonso.explorersaga.ui.composables.mapStyleUrl
import com.alonso.explorersaga.ui.composables.openUrl
import com.alonso.explorersaga.ui.viewmodels.HomeViewModel
import com.alonso.explorersaga.ui.viewmodels.Loadable
import com.alonso.explorersaga.ui.viewmodels.RouteDetail
import com.alonso.explorersaga.ui.viewmodels.RouteViewModel
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@Composable
fun RoutesScreen(viewModel: HomeViewModel, onBack: () -> Unit, onOpenRoute: (String) -> Unit) {
    val routes by viewModel.routes.collectAsStateWithLifecycle()
    val english = isEnglish()
    Scaffold(topBar = { BackTopBar(stringResource(R.string.routes_title), onBack) }) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(routes, key = { it.id }) { route ->
                Card(onClick = { onOpenRoute(route.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (english) route.titleEn else route.titleEs, style = MaterialTheme.typography.titleMedium)
                        Text(
                            (if (english) route.durationEn else route.durationEs) + " · " +
                                stringResource(R.string.route_km, route.km.toInt()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            if (english) route.summaryEn else route.summaryEs,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteDetailScreen(
    viewModel: RouteViewModel,
    darkTheme: Boolean,
    onBack: () -> Unit,
    onOpenPlace: (String) -> Unit
) {
    val state by viewModel.route.collectAsStateWithLifecycle()
    val english = isEnglish()
    val detail = (state as? Loadable.Ready)?.value
    val title = detail?.let { if (english) it.route.titleEn else it.route.titleEs }.orEmpty()

    Scaffold(topBar = { BackTopBar(title, onBack) }) { padding ->
        when {
            state == Loadable.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            detail == null -> Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.detail_missing))
            }
            else -> RouteContent(detail, english, darkTheme, Modifier.padding(padding), onOpenPlace)
        }
    }
}

@Composable
private fun RouteContent(
    detail: RouteDetail,
    english: Boolean,
    darkTheme: Boolean,
    modifier: Modifier,
    onOpenPlace: (String) -> Unit
) {
    val context = LocalContext.current
    val route = detail.route
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            RouteMap(detail.stops, darkTheme, Modifier.fillMaxWidth().height(260.dp))
        }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    (if (english) route.durationEn else route.durationEs) + " · " +
                        stringResource(R.string.route_km, route.km.toInt()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(if (english) route.summaryEn else route.summaryEs, style = MaterialTheme.typography.bodyLarge)
                Button(onClick = { context.openUrl(navigationUrl(detail.stops, route.mode)) }) {
                    Icon(Icons.Filled.Navigation, null)
                    Text(stringResource(R.string.route_navigate), Modifier.padding(start = 8.dp))
                }
                Text(
                    stringResource(R.string.route_stops),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        itemsIndexed(detail.stops) { index, stop ->
            ListItem(
                headlineContent = { Text(if (english) stop.nameEn ?: stop.name else stop.name) },
                supportingContent = stop.muni?.let { { Text(it) } },
                leadingContent = {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
                trailingContent = stop.placeId?.let {
                    { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) }
                },
                modifier = if (stop.placeId != null) {
                    Modifier.clickable { onOpenPlace(stop.placeId) }
                } else Modifier
            )
        }
    }
}

@Composable
private fun RouteMap(stops: List<RouteStopDto>, darkTheme: Boolean, modifier: Modifier) {
    val color = MaterialTheme.colorScheme.primary.toArgb()
    MapLibreView(mapStyleUrl(darkTheme), modifier) { map, style ->
        addRouteLayers(style, stops, color)
        val points = stops.map { LatLng(it.lat, it.lon) }
        if (points.size >= 2) {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds.Builder().includes(points).build(), 64))
        } else if (points.size == 1) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 14.0))
        }
    }
}

private fun addRouteLayers(style: Style, stops: List<RouteStopDto>, color: Int) {
    if (style.getSource("route") != null) return
    val points = stops.map { Point.fromLngLat(it.lon, it.lat) }
    style.addSource(GeoJsonSource("route", Feature.fromGeometry(LineString.fromLngLats(points))))
    style.addSource(
        GeoJsonSource("stops", FeatureCollection.fromFeatures(stops.mapIndexed { index, stop ->
            Feature.fromGeometry(Point.fromLngLat(stop.lon, stop.lat)).apply { addStringProperty("n", "${index + 1}") }
        }))
    )
    style.addLayer(
        LineLayer("route-line", "route").withProperties(
            PropertyFactory.lineColor(color),
            PropertyFactory.lineWidth(4f),
            PropertyFactory.lineDasharray(arrayOf(1.5f, 1.5f)),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND)
        )
    )
    style.addLayer(
        CircleLayer("stops-circle", "stops").withProperties(
            PropertyFactory.circleColor(color),
            PropertyFactory.circleRadius(11f),
            PropertyFactory.circleStrokeWidth(2f),
            PropertyFactory.circleStrokeColor(android.graphics.Color.WHITE)
        )
    )
    style.addLayer(
        SymbolLayer("stops-label", "stops").withProperties(
            PropertyFactory.textField(org.maplibre.android.style.expressions.Expression.get("n")),
            PropertyFactory.textFont(arrayOf("Noto Sans Regular")),
            PropertyFactory.textSize(12f),
            PropertyFactory.textColor(android.graphics.Color.WHITE),
            PropertyFactory.textAllowOverlap(true),
            PropertyFactory.textIgnorePlacement(true)
        )
    )
}

/** Indicaciones en la app de mapas con todas las paradas como puntos intermedios. */
private fun navigationUrl(stops: List<RouteStopDto>, mode: String): String {
    fun point(stop: RouteStopDto) = "${stop.lat},${stop.lon}"
    val waypoints = stops.drop(1).dropLast(1).joinToString("|") { point(it) }
    return buildString {
        append("https://www.google.com/maps/dir/?api=1")
        append("&origin=").append(point(stops.first()))
        append("&destination=").append(point(stops.last()))
        if (waypoints.isNotEmpty()) append("&waypoints=").append(Uri.encode(waypoints))
        append("&travelmode=").append(if (mode == "walk") "walking" else "driving")
    }
}
