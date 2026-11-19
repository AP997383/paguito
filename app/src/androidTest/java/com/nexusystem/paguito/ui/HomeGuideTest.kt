package com.nexusystem.paguito.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexusystem.paguito.ui.components.*
import com.nexusystem.paguito.ui.screens.home.components.HomeGuideSteps
import com.nexusystem.paguito.ui.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class HomeGuideTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun capture(name: String) {
        compose.waitForIdle(); android.os.SystemClock.sleep(500)
        val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(context.getExternalFilesDir("financial-guide-review"), "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun reset(id: String) { context.getSharedPreferences("screen_guides", Context.MODE_PRIVATE).edit().remove(id).commit() }
    @Composable private fun ExampleGuide(id: String) {
        val scroll = rememberScrollState()
        ScreenGuideHost(id, "Guía de Inicio", HomeGuideSteps, scrollState = scroll) {
            Column(Modifier.fillMaxSize().guideViewport().verticalScroll(scroll).padding(20.dp)) {
                HomeGuideSteps.forEach { step ->
                    SoftCard(Modifier.heightIn(min = 220.dp).guideTarget(step.target)) { Text(step.title); Text("Contenido de la sección") }
                    Spacer(Modifier.height(30.dp))
                }
                GuideScrollSpacer()
            }
        }
    }
    @Test fun backdropAndNextAdvanceAllFiveSectionsAndPersistCompletion() {
        val id = "test_full_guide"; reset(id)
        var visible by mutableStateOf(true)
        compose.setContent { PaguitoTheme { if (visible) ExampleGuide(id) } }
        HomeGuideSteps.forEachIndexed { index, step ->
            compose.onNodeWithTag("guide-title").assertTextEquals(step.title)
            compose.waitForIdle()
            // Underlying sections must be brought into the viewport, not merely explained offscreen.
            compose.onNodeWithTag("guide-target-${step.target}", useUnmergedTree = true).assertIsDisplayed()
            if (index == 0) compose.onNodeWithTag("guide-next-area").performTouchInput { click(Offset(4f, height / 2f)) }
            else compose.onNodeWithTag("guide-next").performClick()
        }
        compose.onNodeWithTag("screen-guide").assertDoesNotExist()
        compose.runOnIdle { assertTrue(ScreenGuidePreferences.isCompleted(context, id)); visible = false }
        compose.waitForIdle(); compose.runOnIdle { visible = true }
        compose.onNodeWithTag("screen-guide").assertDoesNotExist()
        reset(id)
    }
    @Test fun skippingEarlyPersistsAndUsesAnIndependentScreenKey() {
        val id = "test_skip_guide"; val other = "test_other_screen"; reset(id); reset(other)
        compose.setContent { PaguitoTheme { ExampleGuide(id) } }
        compose.onNodeWithTag("guide-skip").performClick()
        compose.onNodeWithTag("screen-guide").assertDoesNotExist()
        compose.runOnIdle { assertTrue(ScreenGuidePreferences.isCompleted(context, id)); assertFalse(ScreenGuidePreferences.isCompleted(context, other)) }
        reset(id)
    }
    @Test fun leavingWithoutAcknowledgementShowsGuideAgain() {
        val id = "test_interrupted_guide"; reset(id)
        var visible by mutableStateOf(true)
        compose.setContent { PaguitoTheme { if (visible) ExampleGuide(id) } }
        compose.onNodeWithTag("guide-next").performClick()
        compose.runOnIdle { visible = false }
        compose.waitForIdle()
        compose.runOnIdle { assertFalse(ScreenGuidePreferences.isCompleted(context, id)); visible = true }
        compose.onNodeWithTag("guide-title").assertTextEquals("Tu negocio hoy")
        compose.onNodeWithTag("guide-skip").performClick(); reset(id)
    }
    @Test fun analysisShortcutInvokesItsDestination() {
        var opened = 0
        compose.setContent { PaguitoTheme { AnalysisShortcutCard { opened++ } } }
        compose.onNodeWithTag("open-analysis").performClick()
        compose.runOnIdle { assertEquals(1, opened) }
    }
    @Test fun financialBackgroundSupportsDarkAccentAndDisabledDecoration() {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putBoolean("appearance_waves", true).putString("appearance_accent", AccentColor.Ocean.name).commit()
        compose.setContent { PaguitoTheme(darkTheme = true) { Box(Modifier.fillMaxSize().paguitoBackground()) } }
        capture("financial-background-dark")
        compose.runOnIdle { AppearancePreferences.setWaves(context, false) }
        capture("financial-background-off")
        compose.runOnIdle { assertFalse(AppearancePreferences.read(context.getSharedPreferences("settings", Context.MODE_PRIVATE)).showWaves) }
        compose.runOnIdle { AppearancePreferences.setWaves(context, true); AppearancePreferences.setAccent(context, AccentColor.Paguito) }
    }
}
