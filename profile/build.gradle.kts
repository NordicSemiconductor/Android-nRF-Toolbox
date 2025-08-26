plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile"
}

dependencies {
    implementation(project(":lib_analytics"))
    implementation(project(":profile_data"))
    implementation(project(":lib_ui"))
    implementation(project(":lib_utils"))
    implementation(project(":profile-parsers"))
    api(project(":lib_service"))
    implementation(project(":profile_manager"))
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
    implementation(libs.nordic.blek.client.android)

    implementation(libs.slf4j.timber)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.chart)
    implementation(libs.androidx.compose.material.iconsExtended)

    // DataStore
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)

    // coroutine core
    implementation(libs.kotlinx.coroutines.core)

    // Simple XML
    implementation("org.simpleframework:simple-xml:2.7.1") {
        exclude(group = "stax", module = "stax-api")
        exclude(group = "xpp3", module = "xpp3")
    }
}