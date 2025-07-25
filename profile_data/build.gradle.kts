plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile.data"
}

dependencies {
    implementation(project(":lib_profile"))
    implementation(project(":lib_utils"))
}