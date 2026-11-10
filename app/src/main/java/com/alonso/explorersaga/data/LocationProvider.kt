package com.alonso.explorersaga.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LatLon(val latitude: Double, val longitude: Double)

/**
 * Última ubicación conocida del dispositivo. Se usa solo en el móvil para
 * ordenar por cercanía; nunca se guarda ni se envía.
 */
class LocationProvider(private val context: Context) {

    private val _location = MutableStateFlow<LatLon?>(null)
    val location: StateFlow<LatLon?> = _location.asStateFlow()

    fun hasPermission(): Boolean = PERMISSIONS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun refresh() {
        if (!hasPermission()) return
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        try {
            val best = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            ).filter { manager.isProviderEnabled(it) || it == LocationManager.PASSIVE_PROVIDER }
                .mapNotNull { manager.getLastKnownLocation(it) }
                .maxByOrNull { it.time }
            best?.let { _location.value = LatLon(it.latitude, it.longitude) }
        } catch (_: SecurityException) {
            // El permiso se retiró mientras la app estaba abierta
        }
    }

    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
