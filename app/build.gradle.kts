// Path:
// app/build.gradle.kts

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun localProperty(name: String): String {
    return localProperties.getProperty(name).orEmpty()
}

android {
    namespace = "com.nexusystem.paguito"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.nexusystem.paguito"
        minSdk = 26
        targetSdk = 37
        versionCode = 15
        versionName = "1.4.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
            buildConfigField("String", "BASE_URL", "\"${localProperty("DEV_BASE_URL")}\"")
            buildConfigField("String", "PUBLIC_STORE_URL", "\"https://store-dev.nexusecosystem-mx.com\"")
        }

        create("qa") {
            dimension = "environment"
            buildConfigField("String", "ENVIRONMENT", "\"QA\"")
            buildConfigField("String", "BASE_URL", "\"${localProperty("QA_BASE_URL")}\"")
            buildConfigField("String", "PUBLIC_STORE_URL", "\"https://store-qa.nexusecosystem-mx.com\"")
        }

        create("prod") {
            dimension = "environment"
            buildConfigField("String", "ENVIRONMENT", "\"PROD\"")
            buildConfigField("String", "BASE_URL", "\"${localProperty("PROD_BASE_URL")}\"")
            buildConfigField("String", "PUBLIC_STORE_URL", "\"https://store.nexusecosystem-mx.com\"")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.lottie.compose)
    implementation(libs.coil.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.crashlytics)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    implementation(libs.barcode.scanning)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.zxing.core)

    implementation(libs.gson)
    implementation(libs.play.review)
    implementation(libs.play.review.ktx)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    implementation(libs.security.crypto)
    implementation(libs.billing.ktx)
    implementation(libs.material)
    implementation(libs.play.services.ads)

    implementation(libs.nexus.payments.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}