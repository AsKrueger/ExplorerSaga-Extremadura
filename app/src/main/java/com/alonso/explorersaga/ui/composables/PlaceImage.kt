package com.alonso.explorersaga.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// Resuelve la foto de un lugar para Coil: URL remota, recurso drawable local por nombre,
// o null si el lugar no tiene una foto disponible
@Composable
fun rememberPlaceImageModel(photo: String?): Any? {
    val context = LocalContext.current
    return remember(photo) {
        when {
            photo.isNullOrBlank() -> null
            photo.startsWith("http") -> photo
            else -> context.resources
                .getIdentifier(photo, "drawable", context.packageName)
                .takeIf { it != 0 }
        }
    }
}
