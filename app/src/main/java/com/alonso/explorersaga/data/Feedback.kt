package com.alonso.explorersaga.data

enum class FeedbackType { SUGGESTION, BUG, WRONG_DATA, OTHER }

data class DeviceInfo(
    val appVersion: String,
    val androidVersion: String,
    val device: String,
    val language: String
)

data class FeedbackEmail(
    val recipient: String,
    val subject: String,
    val body: String
)

/**
 * Compone el correo de comentarios. El tipo se escribe en español en el asunto para poder
 * filtrar los correos con independencia del idioma de quien escribe.
 */
object FeedbackComposer {

    fun compose(
        recipient: String,
        type: FeedbackType,
        message: String,
        device: DeviceInfo,
        includeDeviceInfo: Boolean
    ): FeedbackEmail {
        val subject = "[ExplorerSaga] ${type.subjectLabel()}"
        val body = buildString {
            append(message.trim())
            if (includeDeviceInfo) {
                append("\n\n---\n")
                append("ExplorerSaga ${device.appVersion}\n")
                append("Android ${device.androidVersion} · ${device.device}\n")
                append("Idioma: ${device.language}")
            }
        }
        return FeedbackEmail(recipient.trim(), subject, body)
    }

    private fun FeedbackType.subjectLabel() = when (this) {
        FeedbackType.SUGGESTION -> "Sugerencia"
        FeedbackType.BUG -> "Error"
        FeedbackType.WRONG_DATA -> "Datos incorrectos"
        FeedbackType.OTHER -> "Comentario"
    }
}
