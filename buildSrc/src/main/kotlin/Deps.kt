@Suppress("SpellCheckingInspection")
object Deps {

    const val AGP = "com.android.tools.build:gradle:${Versions.AGP}"

    object Compose {
        const val Foundation = "androidx.compose.foundation:foundation:${Versions.Compose}"
        const val Ui = "androidx.compose.ui:ui:${Versions.Compose}"
        const val UiUtil = "androidx.compose.ui:ui-util:${Versions.Compose}"
        const val Material = "androidx.compose.material:material:${Versions.Compose}"
    }

    object AndroidX {
        const val Core = "androidx.core:core-ktx:${Versions.AndroidX.Core}"
        const val AppCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.AppCompat}"
    }

    const val JUnit = "junit:junit:${Versions.JUnit}"
    const val TestExt = "androidx.test.ext:junit:${Versions.TestExt}"
    const val Espresso = "androidx.test.espresso:espresso-core:${Versions.Espresso}"

}
