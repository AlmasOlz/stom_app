plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

val apiBaseUrl: String = providers
    .gradleProperty("apiBaseUrl")
    .orElse("http://10.0.2.2:8000/")
    .get()
val cloudinaryCloudName: String = providers
    .gradleProperty("cloudinaryCloudName")
    .orElse("")
    .get()
val cloudinaryUploadPreset: String = providers
    .gradleProperty("cloudinaryUploadPreset")
    .orElse("")
    .get()

android {
    namespace = "com.example.stomatology.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stomatology.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"$cloudinaryUploadPreset\"")
        buildConfigField("int", "NETWORK_TIMEOUT_SECONDS", "30")
        manifestPlaceholders["MAPS_API_KEY"] = providers
            .gradleProperty("MAPS_API_KEY")
            .orElse("")
            .get()
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("boolean", "LOG_HTTP_BODY", "true")
        }

        getByName("release") {
            isMinifyEnabled = false
            buildConfigField("boolean", "LOG_HTTP_BODY", "false")
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.coil.compose)

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
