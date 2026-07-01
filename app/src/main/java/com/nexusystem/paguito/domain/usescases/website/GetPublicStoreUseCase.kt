package com.nexusystem.paguito.domain.usescases.website

import com.nexusystem.paguito.data.models.responses.PublicStoreData
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import javax.inject.Inject

class GetPublicStoreUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(subdomain: String): PublicStoreData {
        return repository.getPublicStore(subdomain)
    }
}