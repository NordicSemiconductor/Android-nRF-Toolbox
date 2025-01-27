plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.lib.profile"
}

dependencies {
    implementation("no.nordicsemi.kotlin.ble:client-android")
    implementation("no.nordicsemi.kotlin:data:0.3.0")

    // Unit test dependencies
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
}