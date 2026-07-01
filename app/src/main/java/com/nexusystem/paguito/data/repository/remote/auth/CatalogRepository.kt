package com.nexusystem.paguito.data.repository.remote.auth

import com.nexusystem.paguito.data.models.request.SyncCatalogRequest
import com.nexusystem.paguito.data.models.responses.CheckSubdomainData
import com.nexusystem.paguito.data.models.responses.PublicStoreData
import com.nexusystem.paguito.data.remote.cloudFunctions.CatalogApi
import javax.inject.Inject

class CatalogRepository @Inject constructor(
    private val catalogApi: CatalogApi
) {

    suspend fun checkSubdomainAvailability(
        subdomain: String
    ): CheckSubdomainData {
        val response = catalogApi.checkSubdomain(subdomain)

        return when (response.code()) {
            200 -> response.body()?.data ?: throw CatalogException.InvalidResponse()
            409 -> throw CatalogException.SubdomainAlreadyExists()
            400 -> throw CatalogException.InvalidSubdomain()
            else -> throw CatalogException.ServerError()
        }
    }

    suspend fun syncCatalog(
        request: SyncCatalogRequest
    ) {
        val response = catalogApi.syncCatalog(request)

        if (!response.isSuccessful) {
            throw CatalogException.ServerError()
        }
    }

    suspend fun getPublicStore(
        subdomain: String
    ): PublicStoreData {
        val response = catalogApi.publicStore(subdomain)

        return when (response.code()) {
            200 -> response.body()?.data ?: throw CatalogException.InvalidResponse()
            404 -> throw CatalogException.StoreNotFound()
            else -> throw CatalogException.ServerError()
        }
    }

    suspend fun deletePublicStore(
        subdomain: String
    ) {
        val response = catalogApi.deletePublicStore(subdomain)

        when (response.code()) {
            200, 204 -> return
            404 -> throw CatalogException.StoreNotFound()
            else -> throw CatalogException.ServerError()
        }
    }

    suspend fun getPublicStoreByBusinessId(
        businessId: String
    ): PublicStoreData {
        val response = catalogApi.publicStoreByBusinessId(businessId)

        return when (response.code()) {
            200 -> response.body()?.data ?: throw CatalogException.StoreNotFound()
            404 -> throw CatalogException.StoreNotFound()
            else -> throw CatalogException.ServerError()
        }
    }
}