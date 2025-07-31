plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile.parser"
}

dependencies {
    implementation(libs.nordic.blek.client.android)
    implementation(libs.nordic.kotlin.data)

    // Unit test dependencies
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
}