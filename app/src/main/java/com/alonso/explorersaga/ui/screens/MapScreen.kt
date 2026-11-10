package com.alonso.explorersaga.ui.screens

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.alonso.explorersaga.R
import com.alonso.explorersaga.model.Place
import com.alonso.explorersaga.ui.composables.rememberPlaceImageModel
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: PlacesViewModel,
    onFilterClicked: () -> Unit,
    onOpenDetail: (Place) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current


    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            selectedPlace?.let { place ->
                PlaceDetailSheet(place = place, onOpenDetail = { onOpenDetail(place) })
            }
        },
        sheetPeekHeight = 0.dp // Oculta el panel por defecto
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val mapView = rememberMapViewWithLifecycle()

            // osmdroid pinta fuera de su vista si no se recorta (p. ej. bajo la barra de estado)
            AndroidView({ mapView }, modifier = Modifier.fillMaxSize().clipToBounds())

            val filteredPlaces = uiState.places.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            LaunchedEffect(filteredPlaces, uiState.selectedPlace, showSuggestions) {
                // Solo actualiza los marcadores si la búsqueda está vacía o si las sugerencias no están visibles
                if (searchQuery.isBlank() || !showSuggestions) {
                    mapView.updateMarkers(filteredPlaces) { place ->
                        selectedPlace = place
                        scope.launch {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    }
                }

                // Centrar en el lugar seleccionado si viene de otra pantalla
                uiState.selectedPlace?.let { place ->
                    if (filteredPlaces.contains(place)) {
                        mapView.controller.animateTo(GeoPoint(place.latitude, place.longitude), 18.5, 1000L)
                        selectedPlace = place
                        scope.launch {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                        viewModel.clearSelectedPlace() // Limpiar para no volver a centrar
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            showSuggestions = true
                        },
                        label = { Text(stringResource(R.string.map_search_label)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = onFilterClicked,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = stringResource(R.string.cd_filters),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                if (searchQuery.isNotBlank() && showSuggestions) {
                    val placesToShow = filteredPlaces.take(5)
                    if(placesToShow.isNotEmpty()){
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                                )
                        ) {
                            items(placesToShow) { place ->
                                Text(
                                    text = place.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            keyboardController?.hide()
                                            searchQuery = place.name
                                            showSuggestions = false
                                            selectedPlace = place
                                            mapView.controller.animateTo(
                                                GeoPoint(
                                                    place.latitude,
                                                    place.longitude
                                                ), 18.5, 1000L
                                            )
                                            scope.launch {
                                                bottomSheetScaffoldState.bottomSheetState.expand()
                                            }
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Atribución obligatoria de los datos del mapa (licencia ODbL de OpenStreetMap)
            Text(
                text = stringResource(R.string.map_attribution),
                style = MaterialTheme.typography.labelSmall,
                color = Color.DarkGray,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun PlaceDetailSheet(place: Place, onOpenDetail: () -> Unit) {
    val scrollState = rememberScrollState()
    val imageModel = rememberPlaceImageModel(place.photo)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp) // Espacio para el botón
    ) {
        // Imagen y Título
        Box(contentAlignment = Alignment.BottomStart) {
            imageModel?.let {
                AsyncImage(
                    model = it,
                    contentDescription = place.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
        }

        // Contenido
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = place.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Detalles
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                place.horarios?.takeIf { it.isNotBlank() }?.let {
                    Row {
                        Text(stringResource(R.string.detail_hours), fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                        Text(it)
                    }
                }
                place.direccion?.takeIf { it.isNotBlank() }?.let {
                    Row {
                        Text(stringResource(R.string.detail_address), fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                        Text(it)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenDetail,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.map_open_detail))
            }
        }
    }
}

private fun MapView.updateMarkers(places: List<Place>, onMarkerClick: (Place) -> Unit) {
    overlays.clear()

    val colorHistorico = Color(0xFF556B2F).toArgb() // Verde Oliva Oscuro
    val colorGastronomia = Color(0xFFFFC107).toArgb() // Amarillo Ámbar
    val colorTiendas = Color(0xFF1976D2).toArgb()     // Azul

    places.forEach { place ->
        val geoPoint = GeoPoint(place.latitude, place.longitude)
        val marker = Marker(this)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = place.name

        marker.setOnMarkerClickListener { _, _ ->
            onMarkerClick(place)
            true
        }

        val iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_map_pin)?.mutate()

        val catLower = place.category.lowercase()
        val color = when {
            catLower.contains("monument") || catLower.contains("historic") || catLower.contains("iglesia") || catLower.contains("museo") || catLower.contains("religious") || catLower.contains("worship") || catLower.contains("attraction") -> colorHistorico
            catLower.contains("restaurante") || catLower.contains("restaurant") || catLower.contains("cafeteria") || catLower.contains("cafe") || catLower.contains("bar") || catLower.contains("helado") -> colorGastronomia
            catLower.contains("tienda") || catLower.contains("supermercado") || catLower.contains("souvenir") || catLower.contains("shop") || catLower.contains("store") -> colorTiendas
            else -> colorHistorico
        }

        iconDrawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        marker.icon = iconDrawable

        overlays.add(marker)
    }
    invalidate()
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            // Los controles de zoom propios de osmdroid se solapan con la interfaz; se usa el pellizco
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
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
