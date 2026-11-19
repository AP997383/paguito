package com.nexusystem.paguito.ui

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexusystem.paguito.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompactPortraitActivityTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun realActivityRetainsPortraitAfterRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            compose.waitUntil(15000) {
                var portrait = false
                scenario.onActivity { portrait = it.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
                portrait
            }
            scenario.recreate()
            compose.waitForIdle()
            scenario.onActivity { assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, it.requestedOrientation) }
        }
    }
}
