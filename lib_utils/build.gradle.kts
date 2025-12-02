plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.lib.utils"
}

dependencies {
    implementation(nordic.log.timber)
}