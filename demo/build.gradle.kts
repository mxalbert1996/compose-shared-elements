plugins {
    id("com.android.application")
    kotlin("android")
}

android {

    compileSdkVersion(Versions.Sdk.Compile)
    buildToolsVersion = Versions.BuildTools

    buildFeatures.compose = true

    defaultConfig {
        applicationId = "com.mxalbert.sharedelements.demo"
        minSdkVersion(Versions.Sdk.Min)
        targetSdkVersion(Versions.Sdk.Target)
        versionCode = Versions.Project.Code
        versionName = Versions.Project.Name
    }

    buildTypes {
        release {
            postprocessing {
                isRemoveUnusedCode = true
                isRemoveUnusedResources = true
                isObfuscate = false
                isOptimizeCode = true
                proguardFile("proguard-rules.pro")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        useIR = true
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.Compose
    }

}

dependencies {
    implementation(project(":shared-elements"))
    implementation(Deps.AndroidX.Core)
    implementation(Deps.AndroidX.AppCompat)
    implementation(Deps.Compose.Ui)
    implementation(Deps.Compose.Material)
}
