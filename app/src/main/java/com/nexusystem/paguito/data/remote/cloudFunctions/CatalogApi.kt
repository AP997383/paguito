package com.nexusystem.paguito.data.remote.cloudFunctions

import com.nexusystem.paguito.data.models.request.SyncCatalogRequest
import com.nexusystem.paguito.data.models.responses.CheckSubdomainResponse
import com.nexusystem.paguito.data.models.responses.PublicStoreResponse
import com.nexusystem.paguito.data.models.responses.SyncCatalogResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CatalogApi {

    @POST("/v1/paguito/catalog/sync")
    suspend fun syncCatalog(
        @Body request: SyncCatalogRequest
    ): Response<SyncCatalogResponse>

    @GET("/v1/paguito/store/{subdomain}")
    suspend fun publicStore(
        @retrofit2.http.Path("subdomain") subdomain: String
    ): Response<PublicStoreResponse>

    @DELETE("/v1/paguito/store/{subdomain}")
    suspend fun deletePublicStore(
        @retrofit2.http.Path("subdomain") subdomain: String
    ): Response<Unit>

    @GET("/v1/paguito/store/check-subdomain")
    suspend fun checkSubdomain(
        @Query("subdomain") subdomain: String
    ): Response<CheckSubdomainResponse>

    @GET("/v1/paguito/store/by-business")
    suspend fun publicStoreByBusinessId(
        @Query("businessId") businessId: String
    ): Response<PublicStoreResponse>
}