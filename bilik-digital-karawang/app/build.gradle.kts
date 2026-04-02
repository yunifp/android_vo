import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.bit.bilikdigitalkarawang"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bit.bilikdigitalkarawang"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Baca local.properties
        val properties = Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }

        // Inject ke BuildConfig
        buildConfigField("String", "SECRET_KEY", "\"${properties["SECRET_KEY"]}\"")
        buildConfigField("String", "API_BASE_URL", "\"${properties["API_BASE_URL"]}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
    buildFeatures {
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val nav_version = "2.9.3"

    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Hilt
    val hiltVersion = "2.57"
    implementation ("com.google.dagger:hilt-android:$hiltVersion")
    kapt ("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")

    val room_version = "2.7.2"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")
    androidTestImplementation("androidx.room:room-testing:$room_version")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation("io.coil-kt:coil-compose:2.7.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // QR Code Generator
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    // ESC/POS Encoder (DantSu)
    implementation("com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.4.0")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.canopas.intro-showcase-view:introshowcaseview:2.0.1")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // iText 7 untuk PDF
    implementation("com.itextpdf:itext7-core:7.2.5")

    // Atau jika ingin lengkap dengan semua modul:
    implementation("com.itextpdf:kernel:7.2.5")
    implementation("com.itextpdf:layout:7.2.5")
    implementation("com.itextpdf:io:7.2.5")

    implementation("androidx.documentfile:documentfile:1.0.1")

}