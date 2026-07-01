package com.nexusystem.paguito.domain.usescases.website

import com.nexusystem.paguito.data.models.request.SyncCatalogRequest
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import javax.inject.Inject

class SyncCatalogUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(request: SyncCatalogRequest) {
        repository.syncCatalog(request)
    }
}