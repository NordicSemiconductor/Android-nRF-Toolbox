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
    implementation(project(":lib_service"))

    implementation(libs.nordic.core)
    implementation(libs.nordic.navigation)

    implementation(libs.androidx.lifecycle.service)
    implementation(libs.slf4j.timber)
}