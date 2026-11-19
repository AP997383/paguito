package com.nexusystem.paguito.data.local

import android.content.Context
import com.google.gson.Gson
import com.nexusystem.paguito.data.models.responses.PublicStoreData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

fun websiteBusinessId(value: String) = value.trim().lowercase(Locale.ROOT)

/** Public website metadata, stored independently of mutable login/profile data and scoped by owner. */
@Singleton
class WebsiteReferenceStore @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("published_websites", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun read(businessId: String): PublicStoreData? {
        val owner = websiteBusinessId(businessId)
        if (owner.isBlank()) return null
        return runCatching {
            preferences.getString(owner, null)?.let { gson.fromJson(it, PublicStoreData::class.java) }
                ?.takeIf { websiteBusinessId(it.businessId) == owner && it.subdomain.isNotBlank() }
        }.getOrNull()
    }

    fun save(store: PublicStoreData) {
        val owner = websiteBusinessId(store.businessId)
        if (owner.isBlank() || store.subdomain.isBlank()) return
        preferences.edit().putString(owner, gson.toJson(store.copy(businessId = owner, products = store.products.take(2)))).apply()
    }

    fun remove(businessId: String) { preferences.edit().remove(websiteBusinessId(businessId)).apply() }
}
