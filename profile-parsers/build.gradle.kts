plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile.parser"
}

dependencies {
    implementation(nordic.kotlin.data)
    implementation(nordic.blek.client.core.android)

    // Unit test dependencies
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
}