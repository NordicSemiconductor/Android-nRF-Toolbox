plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "no.nordicsemi.android.toolbox.lib.profile"
}

dependencies {
    implementation("no.nordicsemi.kotlin.ble:client-android")
    implementation(libs.slf4j.timber)
    implementation(project(":lib_ui"))
    implementation(project(":lib_service"))

    implementation(libs.nordic.core)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.ui)

    implementation(libs.slf4j.timber)
    implementation(libs.androidx.lifecycle.service)

    implementation(libs.accompanist.permissions)

    //TODO: Remove this once profile is implemented in new BLEK.
    implementation(libs.nordic.blek.client)
}