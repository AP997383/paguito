package com.nexusystem.paguito.domain.usescases.website

import com.nexusystem.paguito.data.models.responses.CheckSubdomainData
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import javax.inject.Inject

class CheckSubdomainUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(subdomain: String): CheckSubdomainData {
        return repository.checkSubdomainAvailability(subdomain)
    }
}