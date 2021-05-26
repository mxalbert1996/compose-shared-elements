dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://androidx.dev/snapshots/builds/7395546/artifacts/repository")
    }
}

rootProject.name = "compose-shared-elements"
include(":demo")
include(":shared-elements")

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
