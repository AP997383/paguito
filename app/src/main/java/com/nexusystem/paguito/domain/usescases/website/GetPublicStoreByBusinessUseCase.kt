package com.nexusystem.paguito.domain.usescases.website

import com.nexusystem.paguito.data.models.responses.PublicStoreData
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import javax.inject.Inject

class GetPublicStoreByBusinessUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(businessId: String): PublicStoreData {
        return repository.getPublicStoreByBusinessId(businessId)
    }
}