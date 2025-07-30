plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.permissions_ranging"
}

dependencies {
    implementation(libs.accompanist.permissions)
}