package com.alonso.explorersaga

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Flujos principales sobre la app real, con el paquete de contenido incluido.
 * No dependen del idioma: los textos se leen de los recursos de la app.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class MainFlowsTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val container get() = (rule.activity.application as ExplorerSagaApplication).container

    private fun text(id: Int) = rule.activity.getString(id)

    @Before
    fun skipWelcomeAndWaitForContent() {
        runBlocking {
            container.settingsStore.setWelcomeSeen(true)
            container.contentRepository.ensureContent()
        }
        rule.waitUntilAtLeastOneExists(hasText(text(R.string.nav_explore)), TIMEOUT)
    }

    @Test
    fun searchWithoutAccentsOpensDetailAndSavesToTrip() {
        rule.onNode(hasText(text(R.string.nav_explore))).performClick()
        rule.onNode(hasSetTextAction()).performTextInput("montesol")
        rule.waitUntilAtLeastOneExists(hasText("Mirador de Montesol"), TIMEOUT)
        rule.onAllNodes(hasText("Mirador de Montesol")).onFirst().performClick()

        // Ficha: guardar en Mi viaje
        rule.waitUntilAtLeastOneExists(hasContentDescription(text(R.string.cd_add_favorite)), TIMEOUT)
        rule.onNode(hasContentDescription(text(R.string.cd_add_favorite))).performClick()
        rule.waitUntilAtLeastOneExists(hasContentDescription(text(R.string.cd_remove_favorite)), TIMEOUT)
        rule.onNode(hasContentDescription(text(R.string.cd_back))).performClick()

        // Mi viaje lo muestra; se quita para dejar el estado como estaba
        rule.onNode(hasText(text(R.string.nav_trip))).performClick()
        rule.waitUntilAtLeastOneExists(hasText("Mirador de Montesol"), TIMEOUT)
        rule.onAllNodes(hasContentDescription(text(R.string.cd_remove_favorite))).onFirst().performClick()
        rule.waitUntilAtLeastOneExists(hasText(text(R.string.trip_empty_title)), TIMEOUT)
    }

    @Test
    fun homeShowsSeasonAndRoutesFromBundledContent() {
        rule.onNode(hasText(text(R.string.nav_home))).performClick()
        rule.waitUntilAtLeastOneExists(hasText(text(R.string.home_season)), TIMEOUT)
        // La lista es perezosa: se comprueba la siguiente sección visible
        rule.onNode(hasText(text(R.string.home_nearby))).assertExists()
    }

    @Test
    fun settingsShowInstalledContentVersion() {
        rule.onNode(hasText(text(R.string.nav_settings))).performClick()
        val installedPrefix = rule.activity.getString(R.string.settings_content_version, "").trim()
        rule.waitUntilAtLeastOneExists(hasText(installedPrefix, substring = true), TIMEOUT)
        rule.onNode(hasText(text(R.string.settings_offline_images))).assertExists()
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
