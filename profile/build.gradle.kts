plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile"
}

dependencies {
    implementation(project(":profile_data"))
    implementation(project(":lib_ui"))
    implementation(project(":lib_profile"))
    implementation(project(":lib_service"))
    implementation(project(":lib_storage"))
    implementation(project(":permissions-ranging"))

    implementation(libs.nordic.core)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.ui)
    implementation(libs.nordic.theme)
    implementation(libs.nordic.permissions.ble)
    implementation(libs.nordic.permissions.notification)
    implementation(libs.nordic.logger)
    implementation(libs.nordic.log.timber)

    implementation(libs.slf4j.timber)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.chart)
    implementation(libs.androidx.compose.material.iconsExtended)

    // DataStore
    implementation("androidx.datastore:datastore-preferences-core:1.1.4")
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    implementation(libs.nordic.blek.client.android)
    // coroutine core
    implementation(libs.kotlinx.coroutines.core)

    // Simple XML
    implementation("org.simpleframework:simple-xml:2.7.1") {
        exclude(group = "stax", module = "stax-api")
        exclude(group = "xpp3", module = "xpp3")
    }
}