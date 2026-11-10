package com.alonso.explorersaga.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.alonso.explorersaga.R

// Licencias, atribuciones y privacidad: necesarias para publicar en Google Play y cumplir las licencias
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
            Text(stringResource(R.string.about_title), style = MaterialTheme.typography.headlineSmall)
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Section(R.string.privacy_title, R.string.privacy_text)
            Section(R.string.about_data_title, R.string.about_data_text)
            TextButton(onClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://www.openstreetmap.org/copyright".toUri()))
                } catch (_: ActivityNotFoundException) {
                    // Sin navegador no hay nada útil que hacer
                }
            }) { Text(stringResource(R.string.about_open_osm)) }
            Section(R.string.about_photo_title, R.string.about_photo_text)
            TextButton(onClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_URL.toUri()))
                } catch (_: ActivityNotFoundException) {
                    // Sin navegador no hay nada útil que hacer
                }
            }) { Text(stringResource(R.string.about_open_privacy)) }
            Section(R.string.about_fonts_title, R.string.about_fonts_text)
            Section(R.string.about_software_title, R.string.about_software_text)
        }
    }
}

private const val PRIVACY_URL =
    "https://askrueger.github.io/ExplorerSaga-Extremadura/privacy/"

@Composable
private fun Section(@StringRes title: Int, @StringRes text: Int) {
    Text(
        stringResource(title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp)
    )
    Text(stringResource(text), style = MaterialTheme.typography.bodyMedium)
}
