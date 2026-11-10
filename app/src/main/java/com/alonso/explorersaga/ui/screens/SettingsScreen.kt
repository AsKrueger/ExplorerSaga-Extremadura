package com.alonso.explorersaga.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.format.Formatter
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.BuildConfig
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.ThemeMode
import com.alonso.explorersaga.ui.viewmodels.SettingsViewModel
import com.alonso.explorersaga.ui.viewmodels.SettingsEvent
import com.alonso.explorersaga.data.OfflineMapState
import com.alonso.explorersaga.data.content.SyncResult
import com.alonso.explorersaga.ui.composables.mapStyleUrl
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Map

private enum class PendingConfirmation { CLEAR_CACHE, CLEAR_DATA, DELETE_MAP }

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    darkTheme: Boolean,
    onOpenFeedback: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val usage by viewModel.storageUsage.collectAsStateWithLifecycle()
    val isWorking by viewModel.isWorking.collectAsStateWithLifecycle()
    val event by viewModel.event.collectAsStateWithLifecycle()
    val contentMeta by viewModel.contentMeta.collectAsStateWithLifecycle()
    val mapState by viewModel.offlineMapState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbar = remember { SnackbarHostState() }
    var confirming by rememberSaveable { mutableStateOf<PendingConfirmation?>(null) }

    LaunchedEffect(Unit) { viewModel.refresh() }
    // El espacio usado cambia al terminar o borrar la descarga del mapa
    LaunchedEffect(mapState is OfflineMapState.Downloaded) { viewModel.refreshStorage() }
    LaunchedEffect(event) {
        val message = when (val current = event) {
            is SettingsEvent.CacheCleared -> resources.getString(
                R.string.settings_clear_cache_done,
                Formatter.formatShortFileSize(context, current.freedBytes)
            )
            SettingsEvent.DataCleared -> resources.getString(R.string.settings_clear_data_done)
            is SettingsEvent.Synced -> resources.getString(
                when (current.result) {
                    SyncResult.UPDATED -> R.string.settings_sync_updated
                    SyncResult.UP_TO_DATE -> R.string.settings_sync_up_to_date
                    SyncResult.FAILED -> R.string.settings_sync_failed
                }
            )
            null -> return@LaunchedEffect
        }
        // Se consume después de mostrarlo: consumirlo antes cambia la clave y cancela este efecto
        snackbar.showSnackbar(message)
        viewModel.consumeEvent()
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            Text(
                stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            GroupTitle(R.string.settings_general)
            LanguageRow()
            ChoiceRow(
                icon = Icons.Filled.DarkMode,
                title = stringResource(R.string.settings_theme),
                options = ThemeMode.entries,
                selected = settings.themeMode,
                label = {
                    when (it) {
                        ThemeMode.SYSTEM -> R.string.theme_system
                        ThemeMode.LIGHT -> R.string.theme_light
                        ThemeMode.DARK -> R.string.theme_dark
                    }
                },
                onSelect = viewModel::setThemeMode
            )

            GroupDivider()
            GroupTitle(R.string.settings_content)
            SettingsRow(
                icon = Icons.Filled.Sync,
                title = stringResource(R.string.settings_sync_now),
                description = contentMeta?.let {
                    stringResource(R.string.settings_content_version, formatVersion(it.version))
                } ?: stringResource(R.string.settings_content_none),
                enabled = !isWorking,
                onClick = viewModel::syncNow
            )
            SwitchRow(
                icon = Icons.Filled.Wifi,
                title = stringResource(R.string.settings_sync_wifi),
                description = stringResource(R.string.settings_sync_wifi_desc),
                checked = settings.syncWifiOnly,
                onCheckedChange = viewModel::setSyncWifiOnly
            )
            val pixelRatio = LocalResources.current.displayMetrics.density
            when (val state = mapState) {
                is OfflineMapState.Downloading -> {
                    SettingsRow(
                        icon = Icons.Filled.CloudDownload,
                        title = stringResource(R.string.settings_offline_map_downloading, (state.progress * 100).toInt()),
                        description = stringResource(R.string.settings_offline_map_desc)
                    )
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
                is OfflineMapState.Downloaded -> SettingsRow(
                    icon = Icons.Filled.Map,
                    title = stringResource(R.string.settings_offline_map_ready),
                    description = stringResource(
                        R.string.settings_offline_map_delete, Formatter.formatShortFileSize(context, state.bytes)
                    ),
                    onClick = { confirming = PendingConfirmation.DELETE_MAP }
                )
                else -> SettingsRow(
                    icon = Icons.Filled.CloudDownload,
                    title = stringResource(
                        if (state == OfflineMapState.Failed) R.string.settings_offline_map_retry
                        else R.string.settings_offline_map
                    ),
                    description = stringResource(R.string.settings_offline_map_desc),
                    enabled = state != OfflineMapState.Unknown,
                    // Primero el estilo en uso; después el otro, que reutiliza las teselas
                    onClick = {
                        viewModel.downloadOfflineMap(listOf(mapStyleUrl(darkTheme), mapStyleUrl(!darkTheme)), pixelRatio)
                    }
                )
            }

            GroupDivider()
            GroupTitle(R.string.settings_storage)
            SwitchRow(
                icon = Icons.Filled.Image,
                title = stringResource(R.string.settings_offline_images),
                description = stringResource(R.string.settings_offline_images_desc),
                checked = settings.offlineImages,
                onCheckedChange = viewModel::setOfflineImages
            )
            SettingsRow(
                icon = Icons.Filled.Storage,
                title = stringResource(R.string.settings_usage),
                description = stringResource(
                    R.string.settings_usage_value,
                    Formatter.formatShortFileSize(context, usage.cacheBytes),
                    Formatter.formatShortFileSize(context, usage.dataBytes)
                )
            )
            SettingsRow(
                icon = Icons.Filled.DeleteSweep,
                title = stringResource(R.string.settings_clear_cache),
                description = stringResource(R.string.settings_clear_cache_desc),
                enabled = !isWorking,
                onClick = { confirming = PendingConfirmation.CLEAR_CACHE }
            )
            SettingsRow(
                icon = Icons.Filled.DeleteForever,
                title = stringResource(R.string.settings_clear_data),
                titleColor = MaterialTheme.colorScheme.error,
                description = stringResource(R.string.settings_clear_data_desc),
                enabled = !isWorking,
                onClick = { confirming = PendingConfirmation.CLEAR_DATA }
            )
            if (isWorking) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }

            GroupDivider()
            GroupTitle(R.string.settings_support)
            SettingsRow(
                icon = Icons.Filled.Feedback,
                title = stringResource(R.string.settings_feedback),
                description = stringResource(R.string.settings_feedback_desc),
                onClick = onOpenFeedback
            )
            SettingsRow(
                icon = Icons.Filled.Star,
                title = stringResource(R.string.settings_rate),
                description = stringResource(R.string.settings_rate_desc),
                onClick = { openStoreListing(context) }
            )
            SettingsRow(
                icon = Icons.Filled.Share,
                title = stringResource(R.string.settings_share),
                description = stringResource(R.string.settings_share_desc),
                onClick = { shareApp(context) }
            )

            GroupDivider()
            GroupTitle(R.string.settings_about)
            SettingsRow(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.settings_licenses),
                description = stringResource(R.string.settings_licenses_desc),
                onClick = onOpenAbout
            )
            Text(
                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    confirming?.let { pending ->
        val (title, text) = when (pending) {
            PendingConfirmation.CLEAR_CACHE ->
                R.string.settings_clear_cache_confirm_title to R.string.settings_clear_cache_confirm_text
            PendingConfirmation.CLEAR_DATA ->
                R.string.settings_clear_data_confirm_title to R.string.settings_clear_data_confirm_text
            PendingConfirmation.DELETE_MAP ->
                R.string.settings_offline_map_confirm_title to R.string.settings_offline_map_confirm_text
        }
        AlertDialog(
            onDismissRequest = { confirming = null },
            title = { Text(stringResource(title)) },
            text = { Text(stringResource(text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirming = null
                    when (pending) {
                        PendingConfirmation.CLEAR_CACHE -> viewModel.clearCache()
                        PendingConfirmation.CLEAR_DATA -> viewModel.clearSavedData()
                        PendingConfirmation.DELETE_MAP -> viewModel.deleteOfflineMap()
                    }
                }) {
                    Text(
                        stringResource(
                            when (pending) {
                                PendingConfirmation.CLEAR_CACHE -> R.string.action_clear_cache
                                PendingConfirmation.CLEAR_DATA -> R.string.action_clear_data
                                PendingConfirmation.DELETE_MAP -> R.string.action_delete
                            }
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirming = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun LanguageRow() {
    // Cadena vacía = idioma del sistema
    val current = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore('-')
    val options = listOf(
        "" to R.string.language_system,
        "es" to R.string.language_es,
        "en" to R.string.language_en
    )
    ChoiceRow(
        icon = Icons.Filled.Language,
        title = stringResource(R.string.settings_language),
        options = options,
        selected = options.firstOrNull { it.first == current } ?: options.first(),
        label = { it.second },
        onSelect = { (tag, _) ->
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    )
}

@Composable
private fun <T> ChoiceRow(
    icon: ImageVector,
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> Int,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        SettingsRow(
            icon = icon,
            title = title,
            description = stringResource(label(selected)),
            onClick = { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(label(option))) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    RowLayout(
        icon = icon,
        title = title,
        description = description,
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        trailing = { Switch(checked = checked, onCheckedChange = null) }
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    description: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    RowLayout(
        icon = icon,
        title = title,
        description = description,
        titleColor = titleColor,
        modifier = if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier
    )
}

@Composable
private fun RowLayout(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            Spacer(Modifier.height(2.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing?.let {
            Spacer(Modifier.width(12.dp))
            it()
        }
    }
}

@Composable
private fun GroupTitle(@StringRes text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(Modifier.padding(top = 8.dp))
}

private fun storeUrl(context: Context) =
    "https://play.google.com/store/apps/details?id=${context.packageName}"

private fun openStoreListing(context: Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri()))
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, storeUrl(context).toUri()))
        } catch (_: ActivityNotFoundException) {
            // Sin tienda ni navegador no hay nada útil que hacer
        }
    }
}

private fun shareApp(context: Context) {
    val intent = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_text, storeUrl(context)))
    context.startActivity(Intent.createChooser(intent, null))
}

// 20261110 -> 10/11/2026
private fun formatVersion(version: Long): String {
    val text = version.toString()
    return if (text.length == 8) "${text.substring(6)}/${text.substring(4, 6)}/${text.substring(0, 4)}" else text
}
