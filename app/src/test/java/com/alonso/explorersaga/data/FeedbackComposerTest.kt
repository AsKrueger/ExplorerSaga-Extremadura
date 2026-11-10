package com.alonso.explorersaga.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackComposerTest {

    private val device = DeviceInfo(
        appVersion = "2.0.0",
        androidVersion = "16",
        device = "Google Pixel 9",
        language = "en-GB"
    )

    @Test
    fun subject_isInSpanishWhateverTheAppLanguage() {
        val email = FeedbackComposer.compose("a@b.es", FeedbackType.WRONG_DATA, "Hola", device, true)

        assertEquals("[ExplorerSaga] Datos incorrectos", email.subject)
    }

    @Test
    fun body_includesDeviceInfoOnlyWhenAllowed() {
        val withInfo = FeedbackComposer.compose("a@b.es", FeedbackType.BUG, "Falla el mapa", device, true)
        val withoutInfo = FeedbackComposer.compose("a@b.es", FeedbackType.BUG, "Falla el mapa", device, false)

        assertTrue(withInfo.body.startsWith("Falla el mapa"))
        assertTrue(withInfo.body.contains("ExplorerSaga 2.0.0"))
        assertTrue(withInfo.body.contains("Android 16 · Google Pixel 9"))
        assertTrue(withInfo.body.contains("en-GB"))
        assertEquals("Falla el mapa", withoutInfo.body)
        assertFalse(withoutInfo.body.contains("Pixel"))
    }

    @Test
    fun messageAndRecipient_areTrimmed() {
        val email = FeedbackComposer.compose("  a@b.es ", FeedbackType.OTHER, "\n  Gracias  \n", device, false)

        assertEquals("a@b.es", email.recipient)
        assertEquals("Gracias", email.body)
    }

    @Test
    fun everyType_hasItsOwnSubject() {
        val subjects = FeedbackType.entries.map {
            FeedbackComposer.compose("", it, "x", device, false).subject
        }

        assertEquals(FeedbackType.entries.size, subjects.toSet().size)
    }
}
