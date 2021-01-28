buildscript {

    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(Deps.AGP)
        classpath(kotlin("gradle-plugin", Versions.Kotlin))
    }

}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
