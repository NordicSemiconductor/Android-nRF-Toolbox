plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile.manager"
}

dependencies {
    implementation(project(":profile_data"))
    implementation(project(":profile-parsers"))
    implementation(project(":lib_utils"))

    implementation(nordic.logger)
    implementation(nordic.log.timber)
    implementation(nordic.blek.client.core.android)

}