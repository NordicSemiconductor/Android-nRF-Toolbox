plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile"
}

dependencies {
    implementation(project(":profile_hts"))
    implementation(project(":lib_ui"))
    implementation(project(":lib_profile"))

    implementation(libs.nordic.navigation)
    implementation(libs.nordic.ui)
    implementation(libs.nordic.permissions.ble)
    implementation("no.nordicsemi.kotlin.ble:client-android")
    
    implementation(libs.slf4j.timber)
}