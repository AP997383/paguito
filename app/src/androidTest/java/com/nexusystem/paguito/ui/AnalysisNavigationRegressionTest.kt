package com.nexusystem.paguito.ui

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexusystem.paguito.MainActivity
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.local.entity.UserSuscriptionData
import com.nexusystem.paguito.ui.screens.home.components.HOME_GUIDE_ID
import com.nexusystem.paguito.utils.OnboardingStore
import com.nexusystem.paguito.utils.PaguitoStore
import com.nexusystem.paguito.utils.SecureStorageManager
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnalysisNavigationRegressionTest {
    @get:Rule val compose = createEmptyComposeRule()
    private fun settle() { compose.mainClock.advanceTimeBy(800); compose.waitForIdle() }
    @Test fun homeButtonAndAnalysisArrowReturnToTheirActualOrigin() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = SecureStorageManager(context)
        val oldProfile = storage.getUserProfile()
        val logged = runBlocking { PaguitoStore.isLoged(context) }
        val invited = runBlocking { PaguitoStore.isInvited(context) }
        storage.saveUserProfile(UserProfileEntity(userSuscription = UserSuscriptionData(isActive = true)))
        context.getSharedPreferences("screen_guides", Context.MODE_PRIVATE).edit().putBoolean(HOME_GUIDE_ID, true).commit()
        runBlocking { OnboardingStore.setOnboardingSeen(context); PaguitoStore.setLoged(context); PaguitoStore.setInvited(context) }
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            compose.waitUntil(15000) { compose.onAllNodesWithTag("open-analysis").fetchSemanticsNodes().isNotEmpty() }
            repeat(2) {
                compose.onNodeWithTag("open-analysis").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }; settle()
                compose.onNodeWithText("Tus estadísticas").assertExists()
                compose.onNodeWithText("Inicio").performClick(); settle()
                compose.onNodeWithText("Todo lo importante, a un toque.").assertExists()
                compose.onNodeWithText("Tus estadísticas").assertDoesNotExist()
            }
            compose.onNodeWithTag("open-analysis").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }; settle()
            compose.onNodeWithContentDescription("Volver").performClick(); settle()
            compose.onNodeWithText("Todo lo importante, a un toque.").assertExists()

            compose.onNode(hasText("Ventas") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick(); settle()
            compose.onNodeWithTag("open-analysis").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }; settle()
            compose.onNodeWithContentDescription("Volver").performClick(); settle()
            compose.onNodeWithText("Tu actividad comercial").assertExists()
            compose.onNodeWithTag("open-analysis").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }; settle()
            compose.onNode(hasText("Ventas") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick(); settle()
            compose.onNodeWithText("Tu actividad comercial").assertExists()
            compose.onNodeWithText("Inicio").performClick(); settle()
            compose.onNodeWithText("Todo lo importante, a un toque.").assertExists()
        } finally {
            scenario.close()
            if (oldProfile != null) storage.saveUserProfile(oldProfile) else storage.clearUserData()
            runBlocking {
                if (logged) PaguitoStore.setLoged(context) else PaguitoStore.setLogout(context)
                if (invited) PaguitoStore.setInvited(context) else PaguitoStore.setInvitedNot(context)
            }
        }
    }
}
