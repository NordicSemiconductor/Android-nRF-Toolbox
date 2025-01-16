plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "no.nordicsemi.android.toolbox.lib.core"
}

dependencies {
    implementation("no.nordicsemi.kotlin.ble:client-android")

    // TODO: This will be removed once the new BLEK library is implemented.
    implementation(libs.nordic.blek.client)

    implementation("no.nordicsemi.kotlin:data:0.3.0")

    // Unit test dependencies
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
}