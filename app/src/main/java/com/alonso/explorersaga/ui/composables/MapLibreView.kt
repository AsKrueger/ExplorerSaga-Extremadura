package com.alonso.explorersaga.ui.composables

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/** Estilos vectoriales de OpenFreeMap (datos de OpenStreetMap, sin claves ni límites). */
fun mapStyleUrl(darkTheme: Boolean): String =
    if (darkTheme) "https://tiles.openfreemap.org/styles/dark" else "https://tiles.openfreemap.org/styles/liberty"

/**
 * Mapa de MapLibre con su ciclo de vida enlazado al de la pantalla.
 * [onStyleLoaded] se llama cada vez que se carga el estilo (al crear y al cambiar de tema).
 */
@Composable
fun MapLibreView(
    styleUrl: String,
    modifier: Modifier = Modifier,
    onStyleLoaded: (MapLibreMap, Style) -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context).apply { onCreate(Bundle()) } }
    val currentOnStyleLoaded by rememberUpdatedState(onStyleLoaded)

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(styleUrl) {
        mapView.getMapAsync { map ->
            map.setStyle(Style.Builder().fromUri(styleUrl)) { style -> currentOnStyleLoaded(map, style) }
        }
    }

    // clipToBounds: sin él la superficie del mapa se dibuja bajo la barra de estado
    AndroidView(factory = { mapView }, modifier = modifier.clipToBounds())
}
