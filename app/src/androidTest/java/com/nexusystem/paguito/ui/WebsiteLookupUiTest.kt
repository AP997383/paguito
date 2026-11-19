package com.nexusystem.paguito.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexusystem.paguito.data.local.WebsiteReferenceStore
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.models.responses.PublicStoreResponse
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import com.nexusystem.paguito.ui.screens.website.WebsiteScreen
import com.nexusystem.paguito.ui.screens.website.viewModel.WebsiteViewModel
import com.nexusystem.paguito.ui.theme.PaguitoTheme
import com.nexusystem.paguito.utils.SecureStorageManager
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class WebsiteLookupUiTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var storage: SecureStorageManager
    private var previous: UserProfileEntity? = null
    private lateinit var cache: WebsiteReferenceStore
    @Before fun setup() {
        storage = SecureStorageManager(context); previous = storage.getUserProfile()
        storage.saveUserProfile(UserProfileEntity(email = WEBSITE_TEST_OWNER))
        cache = WebsiteReferenceStore(context); cache.remove(WEBSITE_TEST_OWNER)
    }
    @After fun cleanup() {
        cache.remove(WEBSITE_TEST_OWNER)
        if (previous != null) storage.saveUserProfile(previous!!) else storage.clearUserData()
    }
    @Test fun lookupFailureShowsRetryAndNeverTheCreateWebsiteFlow() {
        val api = FakeWebsiteApi().apply { businessRead = { throw IOException("offline") } }
        val vm = WebsiteViewModel(CatalogRepository(api, cache), context)
        compose.setContent { PaguitoTheme { WebsiteScreen(onBack = {}, viewModel = vm) } }
        compose.onNodeWithText("Reintentar consulta").assertExists()
        compose.onNodeWithText("Crear sitio web").assertDoesNotExist()
        api.businessRead = { Response.error<PublicStoreResponse>(404, "{}".toResponseBody()) }
        compose.onNodeWithText("Reintentar consulta").performClick()
        compose.onNodeWithText("Crear sitio web").assertExists()
    }
    @Test fun knownWebsiteKeepsItsToolsWhenTheNetworkFails() {
        cache.save(testStore())
        val api = FakeWebsiteApi().apply { businessRead = { throw IOException("offline") } }
        val vm = WebsiteViewModel(CatalogRepository(api, cache), context)
        compose.setContent { PaguitoTheme { WebsiteScreen(onBack = {}, viewModel = vm) } }
        compose.onNodeWithText("Herramientas").assertExists()
        compose.onNodeWithText("Abrir").assertExists()
        compose.onNodeWithText("Crear sitio web").assertDoesNotExist()
    }
}
