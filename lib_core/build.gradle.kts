plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "no.nordicsemi.android.toolbox.lib.core"
}

dependencies {
    implementation(project(":lib_profile"))
}