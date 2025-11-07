package com.alonso.explorersaga.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.R
import com.alonso.explorersaga.model.Place
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    viewModel: PlacesViewModel,
    onFilterClicked: () -> Unit,
    onRoutesClicked: () -> Unit, // TODO
    onCenterLocationClicked: () -> Unit // TODO
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val mapView = rememberMapViewWithLifecycle()

            AndroidView({ mapView })

            LaunchedEffect(uiState.places) {
                mapView.updateMarkers(uiState.places)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = onFilterClicked,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(text = stringResource(id = R.string.map_filter_icon), fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                 FloatingActionButton(
                    onClick = onRoutesClicked,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(text = stringResource(id = R.string.map_routes_icon), fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
             Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                 FloatingActionButton(
                    onClick = onCenterLocationClicked,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Text(text = stringResource(id = R.string.map_center_icon), fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun MapView.updateMarkers(places: List<Place>) {
    this.overlays.clear()
    places.forEach { place ->
        val geoPoint = GeoPoint(place.latitude, place.longitude)
        val marker = Marker(this)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = place.name
        this.overlays.add(marker)
    }
    this.invalidate()
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(38.915, -6.345))
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    return mapView
}
