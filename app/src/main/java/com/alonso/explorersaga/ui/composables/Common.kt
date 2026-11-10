package com.alonso.explorersaga.ui.composables

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.alonso.explorersaga.R
import java.util.Locale

/** true si la interfaz está en inglés (por ajuste de la app o del sistema). */
@Composable
fun isEnglish(): Boolean = LocalConfiguration.current.locales[0].language == Locale.ENGLISH.language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
        actions = { actions() }
    )
}

@Composable
fun formatDistance(meters: Double): String =
    if (meters < 1000) {
        stringResource(R.string.distance_m, meters.toInt())
    } else {
        val locale = LocalLocale.current.platformLocale
        stringResource(R.string.distance_km, String.format(locale, "%.1f", meters / 1000.0))
    }

/** Aviso para pedir la ubicación cuando una sección la necesita. */
@Composable
fun LocationCard(text: String, onRequest: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
            FilledTonalButton(onClick = onRequest) {
                Icon(Icons.Filled.MyLocation, contentDescription = null)
                Text(stringResource(R.string.action_use_location), Modifier.padding(start = 8.dp))
            }
        }
    }
}

// --- Intents ---

fun Context.openUrl(url: String) = launch(Intent(Intent.ACTION_VIEW, url.toUri()))

fun Context.dial(phone: String) =
    launch(Intent(Intent.ACTION_DIAL, "tel:${phone.filter { it.isDigit() || it == '+' }}".toUri()))

/** Abre la app de mapas del móvil con indicaciones hasta el punto. */
fun Context.directions(latitude: Double, longitude: Double, label: String) {
    val geo = "geo:0,0?q=$latitude,$longitude(${android.net.Uri.encode(label)})".toUri()
    if (!launch(Intent(Intent.ACTION_VIEW, geo))) {
        openUrl("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
    }
}

fun Context.shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    launch(Intent.createChooser(intent, null))
}

private fun Context.launch(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
}
