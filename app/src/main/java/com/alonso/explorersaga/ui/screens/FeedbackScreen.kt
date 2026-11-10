package com.alonso.explorersaga.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.alonso.explorersaga.BuildConfig
import com.alonso.explorersaga.R
import com.alonso.explorersaga.data.DeviceInfo
import com.alonso.explorersaga.data.FeedbackComposer
import com.alonso.explorersaga.data.FeedbackType
import kotlinx.coroutines.launch

private val feedbackTypes = listOf(
    FeedbackType.SUGGESTION to R.string.feedback_type_suggestion,
    FeedbackType.BUG to R.string.feedback_type_bug,
    FeedbackType.WRONG_DATA to R.string.feedback_type_wrong_data,
    FeedbackType.OTHER to R.string.feedback_type_other
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val language = LocalConfiguration.current.locales[0].toLanguageTag()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var type by rememberSaveable { mutableStateOf(FeedbackType.SUGGESTION) }
    var message by rememberSaveable { mutableStateOf("") }
    var includeDeviceInfo by rememberSaveable { mutableStateOf(true) }
    val noEmailApp = stringResource(R.string.feedback_no_email_app)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
            Text(stringResource(R.string.feedback_title), style = MaterialTheme.typography.headlineSmall)
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.feedback_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(stringResource(R.string.feedback_type), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                feedbackTypes.forEach { (option, label) ->
                    FilterChip(
                        selected = option == type,
                        onClick = { type = option },
                        label = { Text(stringResource(label)) }
                    )
                }
            }
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(R.string.feedback_message_label)) },
                supportingText = { Text(stringResource(R.string.feedback_message_hint)) },
                minLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = includeDeviceInfo,
                        role = Role.Checkbox,
                        onValueChange = { includeDeviceInfo = it }
                    )
            ) {
                Checkbox(checked = includeDeviceInfo, onCheckedChange = null)
                Text(
                    stringResource(R.string.feedback_include_device),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Button(
                onClick = {
                    val sent = sendFeedback(context, type, message, includeDeviceInfo, language)
                    if (sent) onBack() else scope.launch { snackbar.showSnackbar(noEmailApp) }
                },
                enabled = message.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Text(stringResource(R.string.feedback_send), modifier = Modifier.padding(start = 8.dp))
            }
            Text(
                stringResource(R.string.feedback_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SnackbarHost(snackbar)
    }
}

private fun sendFeedback(
    context: Context,
    type: FeedbackType,
    message: String,
    includeDeviceInfo: Boolean,
    language: String
): Boolean {
    val email = FeedbackComposer.compose(
        recipient = BuildConfig.FEEDBACK_EMAIL,
        type = type,
        message = message,
        device = DeviceInfo(
            appVersion = BuildConfig.VERSION_NAME,
            androidVersion = Build.VERSION.RELEASE,
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            language = language
        ),
        includeDeviceInfo = includeDeviceInfo
    )
    // ACTION_SENDTO con mailto: solo ofrece apps de correo
    val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
        .putExtra(Intent.EXTRA_SUBJECT, email.subject)
        .putExtra(Intent.EXTRA_TEXT, email.body)
    if (email.recipient.isNotEmpty()) intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email.recipient))
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
