package com.nexusystem.paguito.data.models.request

import android.os.Build
import com.nexusystem.paguito.BuildConfig

/** Apertura idempotente con contexto de instalación, sin recibos ni identificadores de hardware privados. */
data class MembershipVisitRequest(
    val email: String,
    val visitId: String,
    val platform: String = "google",
    val installationId: String? = null,
    val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val appVersion: String = BuildConfig.VERSION_NAME,
    val appBuild: String = BuildConfig.VERSION_CODE.toString(),
    val osVersion: String = "Android ${Build.VERSION.RELEASE}",
    val environment: String = BuildConfig.ENVIRONMENT.lowercase(java.util.Locale.ROOT),
    val source: String = "profile_membership"
)
