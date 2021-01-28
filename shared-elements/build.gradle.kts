plugins {
    id("com.android.library")
    kotlin("android")
}

android {

    compileSdkVersion(Versions.Sdk.Compile)
    buildToolsVersion = Versions.BuildTools

    buildFeatures.compose = true

    defaultConfig {
        minSdkVersion(Versions.Sdk.Min)
        targetSdkVersion(Versions.Sdk.Target)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        useIR = true
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = freeCompilerArgs + "-Xinline-classes"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.Compose
    }

}

dependencies {
    implementation(Deps.Compose.Foundation)
    implementation(Deps.Compose.Ui)
    implementation(Deps.Compose.UiUtil)
    implementation(Deps.Compose.Material)

    testImplementation(Deps.JUnit)
    androidTestImplementation(Deps.TestExt)
    androidTestImplementation(Deps.Espresso)
}
