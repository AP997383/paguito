package com.nexusystem.paguito.ui

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.data.local.WebsiteReferenceStore
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.models.request.SyncCatalogRequest
import com.nexusystem.paguito.data.models.responses.*
import com.nexusystem.paguito.data.remote.cloudFunctions.CatalogApi
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import com.nexusystem.paguito.ui.screens.website.setup.viewModel.WebsiteSetupViewModel
import com.nexusystem.paguito.ui.screens.website.viewModel.WebsiteViewModel
import com.nexusystem.paguito.utils.SecureStorageManager
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred

internal const val WEBSITE_TEST_OWNER = "website-reference@example.invalid"
internal const val WEBSITE_TEST_OTHER = "other-website-reference@example.invalid"
internal fun testStore(owner: String = WEBSITE_TEST_OWNER, subdomain: String = "mi-tienda-test") =
    PublicStoreData(owner, subdomain, "Mi tienda de prueba", "5550101234", emptyList())

internal class FakeWebsiteApi : CatalogApi {
    var businessRead: suspend (String) -> Response<PublicStoreResponse> = { Response.success(PublicStoreResponse(true, testStore())) }
    var publicRead: suspend (String) -> Response<PublicStoreResponse> = { Response.success(PublicStoreResponse(true, testStore())) }
    var delete: suspend (String) -> Response<Unit> = { Response.success(Unit) }
    var sync: suspend (SyncCatalogRequest) -> Response<SyncCatalogResponse> = { Response.success(SyncCatalogResponse(true)) }
    val businessIds = mutableListOf<String>()
    val publicReads = mutableListOf<String>()
    val writes = mutableListOf<SyncCatalogRequest>()
    override suspend fun publicStoreByBusinessId(businessId: String): Response<PublicStoreResponse> { businessIds += businessId; return businessRead(businessId) }
    override suspend fun publicStore(subdomain: String): Response<PublicStoreResponse> { publicReads += subdomain; return publicRead(subdomain) }
    override suspend fun syncCatalog(request: SyncCatalogRequest): Response<SyncCatalogResponse> { writes += request; return sync(request) }
    override suspend fun deletePublicStore(subdomain: String) = delete(subdomain)
    override suspend fun checkSubdomain(subdomain: String) = Response.success(CheckSubdomainResponse(true, data = CheckSubdomainData(subdomain, "https://example.invalid/$subdomain")))
}

@RunWith(AndroidJUnit4::class)
class WebsiteReferenceRegressionTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private lateinit var storage: SecureStorageManager
    private var oldProfile: UserProfileEntity? = null
    private lateinit var cache: WebsiteReferenceStore
    private lateinit var api: FakeWebsiteApi
    private lateinit var repository: CatalogRepository

    @Before fun setup() {
        storage = SecureStorageManager(context); oldProfile = storage.getUserProfile()
        storage.saveUserProfile(UserProfileEntity(email = "  Website-Reference@Example.Invalid  "))
        cache = WebsiteReferenceStore(context); cache.remove(WEBSITE_TEST_OWNER); cache.remove(WEBSITE_TEST_OTHER)
        api = FakeWebsiteApi(); repository = CatalogRepository(api, cache)
    }
    @After fun cleanup() {
        cache.remove(WEBSITE_TEST_OWNER); cache.remove(WEBSITE_TEST_OTHER)
        if (oldProfile != null) storage.saveUserProfile(oldProfile!!) else storage.clearUserData()
    }
    private fun main(action: () -> Unit) = instrumentation.runOnMainSync { action() }
    private fun refreshed(): WebsiteViewModel {
        lateinit var vm: WebsiteViewModel
        main { vm = WebsiteViewModel(repository, context); vm.refresh() }
        return vm
    }
    private fun publish(subdomain: String = "mi-tienda-test"): WebsiteSetupViewModel {
        lateinit var vm: WebsiteSetupViewModel
        main {
            vm = WebsiteSetupViewModel(repository, context)
            vm.setupInitialData("Mi tienda de prueba", subdomain, "5550101234", false)
            vm.publishWebsite(listOf(PorductosEntity(id = 1, nombre = "Producto local", precioOriginal = 10f, precioConGanancia = 20f)))
        }
        return vm
    }
    private fun notFound() = Response.error<PublicStoreResponse>(404, "{}".toResponseBody())

    @Test fun publicationPersistsBeforeAnyFollowupReadAndNormalizesIdentity() {
        api.publicRead = { throw IOException("Public GET is temporarily unavailable") }
        val vm = publish()
        assertTrue(vm.uiState.value.showSuccessDialog)
        assertEquals(WEBSITE_TEST_OWNER, api.writes.single().businessId)
        assertTrue(api.publicReads.isEmpty())
        assertEquals("mi-tienda-test", WebsiteReferenceStore(context).read(WEBSITE_TEST_OWNER)?.subdomain)
    }
    @Test fun mixedCaseProfileFindsPublishedWebsiteAfterReopening() {
        val vm = refreshed()
        assertEquals(listOf(WEBSITE_TEST_OWNER), api.businessIds)
        assertTrue(vm.uiState.value.isWebsiteActive)
        assertFalse(vm.uiState.value.canCreateWebsite)
        assertNotNull(WebsiteReferenceStore(context).read(WEBSITE_TEST_OWNER))
    }
    @Test fun offlineLookupPreservesPublishedReferenceAcrossViewModels() {
        cache.save(testStore())
        api.businessRead = { throw IOException("offline") }
        val first = refreshed()
        assertTrue(first.uiState.value.isWebsiteActive)
        assertNotNull(first.uiState.value.errorMessage)
        val reopened = WebsiteViewModel(CatalogRepository(api, WebsiteReferenceStore(context)), context)
        main { reopened.refresh() }
        assertEquals("mi-tienda-test", reopened.uiState.value.publishedWebsite?.subdomain)
        assertFalse(reopened.uiState.value.canCreateWebsite)
    }
    @Test fun cachedLinkRemainsUsableWhileTheServerLookupIsPending() {
        cache.save(testStore())
        val pending = CompletableDeferred<Response<PublicStoreResponse>>()
        api.businessRead = { pending.await() }
        val vm = refreshed()
        assertTrue(vm.uiState.value.isWebsiteActive)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.isRefreshing)
        main { pending.complete(Response.success(PublicStoreResponse(true, testStore()))) }
        assertFalse(vm.uiState.value.isRefreshing)
    }
    @Test fun networkFailureCannotOpenCreationUntilAbsenceIsConfirmed() {
        api.businessRead = { throw IOException("offline") }
        val vm = refreshed()
        assertFalse(vm.uiState.value.canCreateWebsite)
        assertNotNull(vm.uiState.value.errorMessage)
        api.businessRead = { notFound() }
        main { vm.refresh() }
        assertTrue(vm.uiState.value.canCreateWebsite)
        assertNull(vm.uiState.value.errorMessage)
    }
    @Test fun failedBusinessIndexRecoversUsingTheKnownPublicSubdomain() {
        cache.save(testStore())
        api.businessRead = { notFound() }
        val vm = refreshed()
        assertEquals(listOf("mi-tienda-test"), api.publicReads)
        assertTrue(vm.uiState.value.isWebsiteActive)
        assertNotNull(cache.read(WEBSITE_TEST_OWNER))
    }
    @Test fun transientNotFoundCannotDiscardTheKnownWebsiteReference() {
        cache.save(testStore()); cache.save(testStore(WEBSITE_TEST_OTHER, "otra-tienda"))
        api.businessRead = { notFound() }; api.publicRead = { notFound() }
        val vm = refreshed()
        assertFalse(vm.uiState.value.canCreateWebsite)
        assertNotNull(cache.read(WEBSITE_TEST_OWNER))
        assertNotNull(cache.read(WEBSITE_TEST_OTHER))
    }
    @Test fun failedDeleteKeepsReferenceAndSuccessfulDeleteRemovesIt() {
        val vm = refreshed()
        api.delete = { throw IOException("offline") }
        main { vm.deleteWebsite() }
        assertTrue(vm.uiState.value.isWebsiteActive)
        assertNotNull(cache.read(WEBSITE_TEST_OWNER))
        api.delete = { Response.success(Unit) }
        main { vm.deleteWebsite() }
        assertTrue(vm.uiState.value.canCreateWebsite)
        assertNull(cache.read(WEBSITE_TEST_OWNER))
    }
    @Test fun switchingAccountNeverShowsThePreviousAccountsSite() {
        refreshed()
        storage.saveUserProfile(UserProfileEntity(email = WEBSITE_TEST_OTHER))
        api.businessRead = { throw IOException("offline") }
        val other = refreshed()
        assertNull(other.uiState.value.publishedWebsite)
        assertFalse(other.uiState.value.canCreateWebsite)
        assertNotNull(cache.read(WEBSITE_TEST_OWNER))
    }
    @Test fun invalidSuccessResponseIsNotMistakenForAnAbsentWebsite() {
        api.businessRead = { Response.success(PublicStoreResponse(true, data = null)) }
        val vm = refreshed()
        assertFalse(vm.uiState.value.canCreateWebsite)
        assertNotNull(vm.uiState.value.errorMessage)
    }
    @Test fun aSubdomainNowOwnedByAnotherBusinessCannotBeShownAsOurs() {
        cache.save(testStore())
        api.businessRead = { notFound() }
        api.publicRead = { Response.success(PublicStoreResponse(true, testStore(WEBSITE_TEST_OTHER))) }
        val vm = refreshed()
        assertNull(vm.uiState.value.publishedWebsite)
        assertNull(cache.read(WEBSITE_TEST_OWNER))
        assertTrue(vm.uiState.value.canCreateWebsite)
    }
    @Test fun failedPublicationDoesNotReplaceTheExistingReference() {
        cache.save(testStore())
        api.sync = { Response.success(SyncCatalogResponse(false)) }
        val vm = publish("otra-direccion")
        assertFalse(vm.uiState.value.showSuccessDialog)
        assertEquals("mi-tienda-test", cache.read(WEBSITE_TEST_OWNER)?.subdomain)
    }
}
