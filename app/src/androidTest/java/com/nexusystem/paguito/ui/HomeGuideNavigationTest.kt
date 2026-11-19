package com.nexusystem.paguito.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexusystem.paguito.MainActivity
import com.nexusystem.paguito.utils.SecureStorageManager
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.local.entity.UserSuscriptionData
import com.nexusystem.paguito.ui.components.ScreenGuidePreferences
import com.nexusystem.paguito.ui.screens.home.components.*
import com.nexusystem.paguito.utils.OnboardingStore
import com.nexusystem.paguito.utils.PaguitoStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class HomeGuideNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun capture(name: String) {
        compose.waitForIdle(); android.os.SystemClock.sleep(600)
        val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(context.getExternalFilesDir("financial-guide-review"), "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @Test fun homeTourRevealsRealSectionsAndBothShortcutsOpenAnalysis() {
        val storage = SecureStorageManager(context)
        val savedProfile = storage.getUserProfile()
        storage.saveUserProfile(UserProfileEntity(userSuscription = UserSuscriptionData(isActive = true)))
        val wasLogged = runBlocking { PaguitoStore.isLoged(context) }
        val wasInvited = runBlocking { PaguitoStore.isInvited(context) }
        context.getSharedPreferences("screen_guides", Context.MODE_PRIVATE).edit().remove(HOME_GUIDE_ID).commit()
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString("appearance_accent", "Paguito").putString("appearance_mode", "Light").putBoolean("appearance_waves", true).commit()
        runBlocking { OnboardingStore.setOnboardingSeen(context); PaguitoStore.setLoged(context); PaguitoStore.setInvited(context) }
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            compose.waitUntil(15000) { compose.onAllNodesWithTag("guide-title").fetchSemanticsNodes().isNotEmpty() }
            HomeGuideSteps.forEachIndexed { index, step ->
                compose.onNodeWithTag("guide-title").assertTextEquals(step.title)
                compose.onNodeWithTag("guide-target-${step.target}", useUnmergedTree = true).assertIsDisplayed()
                capture("home-step-${index + 1}")
                val target = compose.onNodeWithTag("guide-target-${step.target}", useUnmergedTree = true).fetchSemanticsNode().boundsInWindow
                val panel = compose.onNodeWithTag("guide-panel").fetchSemanticsNode().boundsInWindow
                assertTrue("The explanation must be below ${step.title}", target.bottom <= panel.top)
                assertTrue("The section must align near the top: ${step.title}", target.top < 320f)
                val pointer = compose.onNodeWithTag("guide-pointer").fetchSemanticsNode().boundsInWindow
                assertTrue("The arrow must connect the section to its explanation", pointer.top >= target.bottom && pointer.bottom <= panel.top)
                if (index == 1) compose.onNodeWithTag("guide-next-area").performTouchInput { click(Offset(4f, height / 2f)) }
                else compose.onNodeWithTag("guide-next").performClick()
            }
            compose.onNodeWithTag("screen-guide").assertDoesNotExist()
            compose.runOnIdle { assertTrue(ScreenGuidePreferences.isCompleted(context, HOME_GUIDE_ID)) }
            compose.onNodeWithTag("open-analysis").performScrollTo().performClick()
            compose.onNodeWithText("Tus estadísticas").assertExists()
            compose.onNodeWithContentDescription("Volver").performClick()
            compose.onNodeWithTag("screen-guide").assertDoesNotExist()
            compose.onNodeWithText("Ventas").performClick()
            compose.onNodeWithTag("open-analysis").performScrollTo().performClick()
            compose.onNodeWithText("Tus estadísticas").assertExists()
            compose.onNodeWithContentDescription("Volver").performClick()
            compose.onNodeWithText("Tu actividad comercial").assertExists()
            capture("sales-analysis-shortcut")
            compose.onNodeWithText("Inicio").performClick()
            compose.onNodeWithTag("screen-guide").assertDoesNotExist()
        } finally {
            scenario.close()
            if (savedProfile != null) storage.saveUserProfile(savedProfile) else storage.clearUserData()
            runBlocking {
                if (wasLogged) PaguitoStore.setLoged(context) else PaguitoStore.setLogout(context)
                if (wasInvited) PaguitoStore.setInvited(context) else PaguitoStore.setInvitedNot(context)
            }
        }
    }
}
