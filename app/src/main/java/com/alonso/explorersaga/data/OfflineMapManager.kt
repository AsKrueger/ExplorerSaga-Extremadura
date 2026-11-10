package com.alonso.explorersaga.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import kotlin.coroutines.resume

sealed interface OfflineMapState {
    data object Unknown : OfflineMapState
    data object NotDownloaded : OfflineMapState
    data class Downloading(val progress: Float) : OfflineMapState
    data class Downloaded(val bytes: Long) : OfflineMapState
    data object Failed : OfflineMapState
}

/**
 * Mapa vectorial de toda Extremadura para usarlo sin conexión, con los estilos
 * claro y oscuro (comparten teselas, así que el segundo apenas ocupa). Las
 * teselas vienen de OpenFreeMap, que permite su uso sin límites ni claves.
 */
class OfflineMapManager(private val context: Context) {

    private val _state = MutableStateFlow<OfflineMapState>(OfflineMapState.Unknown)
    val state: StateFlow<OfflineMapState> = _state.asStateFlow()

    private val main = Handler(Looper.getMainLooper())

    private val manager: OfflineManager by lazy {
        MapLibre.getInstance(context)
        OfflineManager.getInstance(context)
    }

    fun refresh() = main.post {
        findRegions { regions ->
            if (regions.isEmpty()) {
                _state.value = OfflineMapState.NotDownloaded
                return@findRegions
            }
            statuses(regions) { statuses ->
                val pending = regions.zip(statuses).firstOrNull { (_, status) -> status?.isComplete != true }
                if (pending == null) {
                    // Las regiones comparten teselas: se informa del tamaño real en disco
                    _state.value = OfflineMapState.Downloaded(diskBytes())
                } else {
                    // Descarga interrumpida (p. ej. se cerró la app): se reanuda
                    resume(pending.first, regions.indexOf(pending.first), regions.size, onDone = ::refresh)
                }
            }
        }
    }

    fun download(styleUrls: List<String>, pixelRatio: Float) = main.post {
        _state.value = OfflineMapState.Downloading(0f)
        createNext(styleUrls, 0, pixelRatio)
    }

    private fun createNext(styleUrls: List<String>, index: Int, pixelRatio: Float) {
        if (index == styleUrls.size) {
            refresh()
            return
        }
        val definition = OfflineTilePyramidRegionDefinition(
            styleUrls[index], EXTREMADURA_BOUNDS, MIN_ZOOM, MAX_ZOOM, pixelRatio
        )
        manager.createOfflineRegion(definition, "$REGION_NAME-$index".toByteArray(),
            object : OfflineManager.CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    resume(offlineRegion, index, styleUrls.size) { createNext(styleUrls, index + 1, pixelRatio) }
                }

                override fun onError(error: String) {
                    _state.value = OfflineMapState.Failed
                }
            })
    }

    /** Borra el mapa descargado (todas sus regiones). */
    suspend fun delete() = suspendCancellableCoroutine { continuation ->
        main.post {
            findRegions { regions -> deleteAll(regions) { continuation.resume(Unit) } }
        }
    }

    private fun deleteAll(regions: List<OfflineRegion>, onDone: () -> Unit) {
        val region = regions.firstOrNull()
        if (region == null) {
            _state.value = OfflineMapState.NotDownloaded
            onDone()
            return
        }
        region.setDownloadState(OfflineRegion.STATE_INACTIVE)
        region.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
            override fun onDelete() = deleteAll(regions.drop(1), onDone)
            override fun onError(error: String) = deleteAll(regions.drop(1), onDone)
        })
    }

    /** Vacía la caché automática de teselas vistas (no toca el mapa descargado). */
    suspend fun clearAmbientCache() = suspendCancellableCoroutine { continuation ->
        main.post {
            manager.clearAmbientCache(object : OfflineManager.FileSourceCallback {
                override fun onSuccess() = continuation.resume(Unit)
                override fun onError(message: String) = continuation.resume(Unit)
            })
        }
    }

    private fun resume(region: OfflineRegion, index: Int, count: Int, onDone: () -> Unit) {
        region.setObserver(object : OfflineRegion.OfflineRegionObserver {
            override fun onStatusChanged(status: OfflineRegionStatus) {
                if (status.isComplete) {
                    region.setObserver(null)
                    region.setDownloadState(OfflineRegion.STATE_INACTIVE)
                    onDone()
                } else if (status.requiredResourceCount > 0) {
                    val partial = status.completedResourceCount.toFloat() / status.requiredResourceCount
                    _state.value = OfflineMapState.Downloading((index + partial) / count)
                }
            }

            override fun onError(error: OfflineRegionError) {
                // Errores de red puntuales: MapLibre reintenta solo
            }

            override fun mapboxTileCountLimitExceeded(limit: Long) {
                region.setDownloadState(OfflineRegion.STATE_INACTIVE)
                _state.value = OfflineMapState.Failed
            }
        })
        region.setDownloadState(OfflineRegion.STATE_ACTIVE)
    }

    private fun statuses(regions: List<OfflineRegion>, onResult: (List<OfflineRegionStatus?>) -> Unit) {
        val results = arrayOfNulls<OfflineRegionStatus>(regions.size)
        var remaining = regions.size
        regions.forEachIndexed { i, region ->
            region.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
                override fun onStatus(status: OfflineRegionStatus?) {
                    results[i] = status
                    if (--remaining == 0) onResult(results.toList())
                }

                override fun onError(error: String?) {
                    if (--remaining == 0) onResult(results.toList())
                }
            })
        }
    }

    private fun findRegions(onResult: (List<OfflineRegion>) -> Unit) {
        manager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                onResult(offlineRegions.orEmpty()
                    .filter { it.metadata.decodeToString().startsWith(REGION_NAME) }
                    .sortedBy { it.metadata.decodeToString() })
            }

            override fun onError(error: String) {
                onResult(emptyList())
            }
        })
    }

    private fun diskBytes(): Long = context.filesDir
        .listFiles { file -> file.name.startsWith("mbgl-offline") }
        ?.sumOf { it.length() } ?: 0

    companion object {
        private const val REGION_NAME = "extremadura"
        private const val MIN_ZOOM = 5.0
        // Nivel 13 basta para leer calles y caminos; MapLibre sobreescala a partir de ahí
        private const val MAX_ZOOM = 13.0
        val EXTREMADURA_BOUNDS: LatLngBounds = LatLngBounds.from(40.49, -4.64, 37.94, -7.55)
    }
}
