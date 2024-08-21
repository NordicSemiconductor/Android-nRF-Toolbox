plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.lib.profile"
}

dependencies {
    implementation("no.nordicsemi.kotlin.ble:client-android")
    implementation(libs.slf4j.timber)
    implementation(project(":lib_ui"))
}