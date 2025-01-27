plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile"
}

dependencies {
    implementation(project(":lib_core"))
    implementation(project(":lib_ui"))
    implementation(project(":lib_service"))

    implementation(libs.nordic.core)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.ui)
    implementation(libs.nordic.theme)
    implementation(libs.nordic.permissions.ble)
    implementation(libs.nordic.logger)
    implementation(libs.nordic.log.timber)

    implementation(libs.slf4j.timber)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.chart)
    implementation(libs.androidx.compose.material.iconsExtended)

    implementation("no.nordicsemi.kotlin.ble:client-android")
}