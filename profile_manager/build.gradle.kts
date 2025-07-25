plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile.manager"
}

dependencies {
    implementation(project(":profile_data"))
    implementation(project(":lib_profile"))
    implementation(project(":lib_utils"))

    implementation(libs.nordic.logger)
    implementation(libs.nordic.log.timber)
    implementation(libs.nordic.blek.client.android)

}