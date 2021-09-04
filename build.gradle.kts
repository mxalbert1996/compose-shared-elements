buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.agp)
        classpath(libs.kotlin.gradle)
        classpath(libs.maven.publish)
    }

}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
