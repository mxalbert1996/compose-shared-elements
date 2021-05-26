buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-beta02")
        classpath(kotlin("gradle-plugin", "1.5.10"))
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.15.1")
    }

}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
