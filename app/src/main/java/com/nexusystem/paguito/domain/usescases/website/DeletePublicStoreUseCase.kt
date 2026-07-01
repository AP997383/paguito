package com.nexusystem.paguito.domain.usescases.website

import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import javax.inject.Inject

class DeletePublicStoreUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(subdomain: String) {
        repository.deletePublicStore(subdomain)
    }
}