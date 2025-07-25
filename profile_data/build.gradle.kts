plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile.data"
}

dependencies {
    implementation(project(":profile-parsers"))
    implementation(project(":lib_utils"))
}