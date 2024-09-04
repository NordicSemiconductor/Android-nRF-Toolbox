plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.lib.profile"
}

dependencies {
    implementation("no.nordicsemi.kotlin.ble:client-android")
    implementation(project(":lib_ui"))
    implementation(project(":lib_service"))

    implementation(libs.nordic.core)
    // TODO: Once the profile is implemented on the new version of BLEK, this dependency should be removed
    implementation(libs.nordic.blek.client)

    implementation(libs.androidx.lifecycle.service)
    implementation(libs.slf4j.timber)
}