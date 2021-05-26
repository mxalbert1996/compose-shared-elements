plugins {
    id("com.android.library")
    kotlin("android")
}

android {

    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    buildFeatures.compose = true

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

}

dependencies {
    implementation(libs.bundles.compose)

    testImplementation(libs.jUnit)
    androidTestImplementation(libs.testExt)
    androidTestImplementation(libs.espresso)
}
