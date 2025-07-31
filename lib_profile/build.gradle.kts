plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.lib.profile"
}

dependencies {
    implementation(libs.nordic.blek.client.android)
    implementation("no.nordicsemi.kotlin:data:0.4.0")

    // Unit test dependencies
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
}