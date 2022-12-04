import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.vanniktech.maven.publish.base")
}

android {
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    buildFeatures {
        buildConfig = false
        compose = true
    }

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
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    testImplementation(libs.jUnit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.testExt)
}

mavenPublishing {
    group = project.property("GROUP") ?: group
    version = project.property("VERSION_NAME") ?: version
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
    pomFromGradleProperties()
    configure(AndroidSingleVariantLibrary())
}

publishing {
    repositories {
        maven("$buildDir/repos/snapshots")
    }
}
