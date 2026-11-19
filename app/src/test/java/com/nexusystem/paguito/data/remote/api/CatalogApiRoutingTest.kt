package com.nexusystem.paguito.data.remote.api

import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.data.di.NetworkModule
import com.nexusystem.paguito.data.models.request.SyncCatalogRequest
import com.nexusystem.paguito.data.remote.cloudFunctions.CatalogApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifica las cinco rutas con el cliente real de catálogo, sin escribir ni consultar servicios remotos. */
class CatalogApiRoutingTest {
    /** Un host público configurado para abrir páginas nunca debe recibir solicitudes de los servicios del catálogo. */
    @Test
    fun `all catalog operations use environment API host and preserve routes`() = runBlocking {
        val requests = mutableListOf<Request>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            requests += chain.request()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{\"success\":true}".toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        val api = NetworkModule.provideAwsRetrofit(client).create(CatalogApi::class.java)

        api.syncCatalog(SyncCatalogRequest("business-test", "route-test", "Prueba", "", emptyList()))
        api.publicStore("route-test")
        api.deletePublicStore("route-test")
        api.checkSubdomain("route-test")
        api.publicStoreByBusinessId("business-test")

        val expectedHost = when (BuildConfig.ENVIRONMENT) {
            "DEV" -> "api-dev.nexusecosystem-mx.com"
            "QA" -> "api-qa.nexusecosystem-mx.com"
            "PROD" -> "api.nexusecosystem-mx.com"
            else -> error("Entorno de catálogo desconocido")
        }
        assertEquals(5, requests.size)
        requests.forEach { request ->
            assertEquals("https", request.url.scheme)
            assertEquals(expectedHost, request.url.host)
        }
        assertEquals(listOf("POST", "GET", "DELETE", "GET", "GET"), requests.map { it.method })
        assertEquals(
            listOf(
                "/v1/paguito/catalog/sync",
                "/v1/paguito/store/route-test",
                "/v1/paguito/store/route-test",
                "/v1/paguito/store/check-subdomain",
                "/v1/paguito/store/by-business"
            ),
            requests.map { it.url.encodedPath }
        )
        assertEquals("route-test", requests[3].url.queryParameter("subdomain"))
        assertEquals("business-test", requests[4].url.queryParameter("businessId"))
    }
}
