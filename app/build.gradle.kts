import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrainsKotlinSerialization)
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
    id("com.google.firebase.crashlytics")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun localProperty(name: String): String {
    return localProperties.getProperty(name) ?: ""
}

android {
    namespace = "com.nexusystem.paguito"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nexusystem.paguito"
        minSdk = 24
        targetSdk = 36
        versionCode = 13
        versionName = "1.3.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    configurations.all {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
            force("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
        }
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.constrain.layout)

    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.airbnb.android:lottie-compose:6.3.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-crashlytics")

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation("androidx.hilt:hilt-work:1.2.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Room
    implementation("androidx.room:room-runtime:2.8.3")
    kapt("androidx.room:room-compiler:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")

    // Images
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Camera / Barcode
    implementation(libs.barcode.scanning)
    implementation("androidx.camera:camera-core:1.5.2")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.5.2")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.zxing:core:3.5.3")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Play Review
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")

    // Retrofit / OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Secure Storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Billing
    implementation("com.android.billingclient:billing-ktx:7.0.0")

    // Material
    implementation("com.google.android.material:material:1.11.0")

    // AdMob
    implementation("com.google.android.gms:play-services-ads:23.4.0")
}