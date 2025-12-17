import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ktlint)
}
val properties =
    Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }


android {
    namespace = "com.ion.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.ion.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"http://3.38.149.207:8080/\"")

        val kakaoNativeAppKey = properties["kakao.native.app.key"].toString()

        buildConfigField(
            "String",
            "KAKAO_NATIVE_APP_KEY",
            "\"$kakaoNativeAppKey\""
        )
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoNativeAppKey


        buildConfigField("String", "ION_ASKING", properties["ion.asking"].toString())
        buildConfigField("String", "ION_SERVICE", properties["ion.service"].toString())
        buildConfigField("String", "ION_PRIVACY_POLICY", properties["ion.privacy.policy"].toString())
        buildConfigField("String", "ION_TERMS_OF_SERVICE", properties["ion.terms.of.service"].toString())
        buildConfigField("String", "MASTER_KEY", properties["masterkey"].toString())
    }

    buildTypes {
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
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.ktx)
    implementation(libs.room.ktx)
    implementation(libs.foundation.layout)
    // Test
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.bundles.test)

    // Debug
    debugImplementation(libs.bundles.debug)

    // AndroidX
    implementation(libs.bundles.androidx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.browser)

    // Google
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.crashlytics)

    // Network
    implementation(platform(libs.okhttp.bom))
    implementation(libs.bundles.okhttp)
    implementation(libs.bundles.retrofit)
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    // Coil
    implementation(libs.coil.compose)

    // Timber
    implementation(libs.timber)

    // Kakao Login
    implementation(libs.bundles.kakao)

    // Ui
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.lottie.compose)
    implementation("com.github.tehras:charts:0.2.4-alpha")
    debugImplementation(libs.androidx.ui.tooling)
}

ktlint {
    android = true
    debug = true
    coloredOutput = true
    verbose = true
    outputToConsole = true
}
