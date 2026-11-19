package com.nexusystem.paguito.data.repository.remote.auth

import com.nexusystem.paguito.data.local.WebsiteReferenceStore
import com.nexusystem.paguito.data.local.websiteBusinessId
import com.nexusystem.paguito.data.models.request.SyncCatalogRequest
import com.nexusystem.paguito.data.models.responses.*
import com.nexusystem.paguito.data.remote.cloudFunctions.CatalogApi
import java.util.Locale
import javax.inject.Inject

class CatalogRepository @Inject constructor(private val catalogApi: CatalogApi, private val references: WebsiteReferenceStore) {
    fun cachedPublicStore(businessId: String) = references.read(businessId)
    fun rememberPublicStore(store: PublicStoreData) = references.save(store)
    fun forgetPublicStore(businessId: String) = references.remove(businessId)

    suspend fun checkSubdomainAvailability(subdomain: String): CheckSubdomainData {
        val response = catalogApi.checkSubdomain(subdomain)
        return when (response.code()) {
            200 -> response.body()?.data ?: throw CatalogException.InvalidResponse()
            409 -> throw CatalogException.SubdomainAlreadyExists()
            400 -> throw CatalogException.InvalidSubdomain()
            else -> throw CatalogException.ServerError()
        }
    }

    suspend fun syncCatalog(request: SyncCatalogRequest): PublicStoreData {
        val normalized = request.copy(businessId = websiteBusinessId(request.businessId), subdomain = request.subdomain.trim().lowercase(Locale.ROOT))
        val response = catalogApi.syncCatalog(normalized)
        if (!response.isSuccessful || response.body()?.success == false) throw CatalogException.ServerError()
        val store = PublicStoreData(normalized.businessId, normalized.subdomain, normalized.businessName, normalized.whatsapp,
            normalized.products.map { PublicStoreProduct(it.id, it.name, it.imageUrl, it.originalPrice, it.finalPrice, it.stock, it.notes, it.sales) })
        // The successful write is the acknowledgement. A second GET must not turn it into a failed publication.
        references.save(store)
        return store
    }

    suspend fun getPublicStore(subdomain: String): PublicStoreData {
        val response = catalogApi.publicStore(subdomain)
        return when (response.code()) {
            200 -> response.body()?.takeIf { it.success }?.data ?: throw CatalogException.InvalidResponse()
            404 -> throw CatalogException.StoreNotFound()
            else -> throw CatalogException.ServerError()
        }
    }

    suspend fun deletePublicStore(subdomain: String) {
        val response = catalogApi.deletePublicStore(subdomain)
        when (response.code()) {
            200, 204 -> return
            404 -> throw CatalogException.StoreNotFound()
            else -> throw CatalogException.ServerError()
        }
    }

    suspend fun getPublicStoreByBusinessId(businessId: String): PublicStoreData {
        val owner = websiteBusinessId(businessId)
        val response = catalogApi.publicStoreByBusinessId(owner)
        return when (response.code()) {
            200 -> {
                val store = response.body()?.takeIf { it.success }?.data ?: throw CatalogException.InvalidResponse()
                if (websiteBusinessId(store.businessId) != owner || store.subdomain.isBlank()) throw CatalogException.InvalidResponse()
                references.save(store)
                store
            }
            404 -> throw CatalogException.StoreNotFound()
            else -> throw CatalogException.ServerError()
        }
    }
}
