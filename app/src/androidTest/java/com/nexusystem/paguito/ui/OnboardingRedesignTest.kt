package com.nexusystem.paguito.ui

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexusystem.paguito.ui.components.requestedAppOrientation
import com.nexusystem.paguito.ui.screens.onboarding.OnboardingScreen
import com.nexusystem.paguito.ui.theme.PaguitoTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class OnboardingRedesignTest {
    @get:Rule val compose = createComposeRule()
    private fun capture(name: String) {
        compose.waitForIdle(); android.os.SystemClock.sleep(500)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        File(instrumentation.targetContext.getExternalFilesDir("onboarding-review"), "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @Test fun fourPagesFinishOnceAndRenderAllScenes() {
        var finished = 0
        compose.setContent { PaguitoTheme(darkTheme = false) { Box(Modifier.statusBarsPadding()) { OnboardingScreen { finished++ } } } }
        repeat(4) { page ->
            compose.onNodeWithTag("onboarding-page-count").assertTextEquals("${page + 1} / 4")
            compose.onNodeWithTag("onboarding-next").assertIsDisplayed()
            capture("onboarding-${page + 1}")
            compose.onNodeWithTag("onboarding-next").performClick()
        }
        compose.runOnIdle { assertEquals(1, finished) }
        compose.onNodeWithTag("onboarding-next").assertIsNotEnabled()
        compose.onNodeWithTag("onboarding-skip").assertIsNotEnabled()
    }
    @Test fun swipePreviousDotsAndRestorationPreserveNavigation() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { PaguitoTheme { OnboardingScreen {} } }
        compose.onNodeWithTag("onboarding-pager").performTouchInput { swipeLeft() }
        compose.onNodeWithTag("onboarding-page-count").assertTextEquals("2 / 4")
        compose.onNodeWithTag("onboarding-previous").performClick()
        compose.onNodeWithTag("onboarding-page-count").assertTextEquals("1 / 4")
        compose.onNodeWithTag("onboarding-dot-2").performClick()
        compose.onNodeWithTag("onboarding-page-count").assertTextEquals("3 / 4")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("onboarding-page-count").assertTextEquals("3 / 4")
    }
    @Test fun skipWorksInShortDarkWindow() {
        var finished = 0
        compose.setContent { PaguitoTheme(darkTheme = true) { Box(Modifier.height(440.dp)) { OnboardingScreen { finished++ } } } }
        compose.onNodeWithTag("onboarding-next").assertIsDisplayed().performClick()
        compose.onNodeWithTag("onboarding-page-count").assertTextEquals("2 / 4")
        compose.onNodeWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(com.nexusystem.paguito.R.string.intro_detail_clients)).performScrollTo().assertIsDisplayed()
        capture("onboarding-dark-short")
        compose.onNodeWithTag("onboarding-skip").performClick()
        compose.runOnIdle { assertEquals(1, finished) }
    }
    @Test fun phonesStayPortraitAndLargeWindowsFollowSystem() {
        listOf(390 to 840, 840 to 390, 599 to 1000).forEach { (w,h) -> assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, requestedAppOrientation(w,h)) }
        listOf(600 to 960, 960 to 600, 840 to 1200).forEach { (w,h) -> assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, requestedAppOrientation(w,h)) }
    }
}
