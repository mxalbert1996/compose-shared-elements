buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(Deps.AGP)
        classpath(kotlin("gradle-plugin", Versions.Kotlin))
    }

}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()  // kotlinx.collections.immutable needs this
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
