/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
plugins {
    alias(libs.plugins.nordic.application.compose)
    alias(libs.plugins.nordic.hilt)
}

if (getGradle().getStartParameter().getTaskRequests().toString().contains("Release")) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

android {
    namespace = "no.nordicsemi.android.nrftoolbox"
}

dependencies {
    //Hilt requires to implement every module in the main app module
    //https://github.com/google/dagger/issues/2123
    implementation(project(":profile_bps"))
    implementation(project(":profile_csc"))
    implementation(project(":profile_cgms"))
    implementation(project(":profile_gls"))
    implementation(project(":profile_hrs"))
    implementation(project(":profile_hts"))
    implementation(project(":profile_prx"))
    implementation(project(":profile_rscs"))

    implementation(project(":profile_uart"))

    implementation(project(":lib_analytics"))
    implementation(project(":lib_ui"))
    implementation(project(":lib_utils"))
    implementation(project(":lib_service"))
    implementation(project(":lib_scanner"))

    implementation(libs.nordic.core)
    implementation(libs.nordic.theme)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.blek.uiscanner)
    implementation(libs.nordic.uilogger)
    implementation(libs.nordic.permissions.ble)
    implementation(libs.nordic.analytics)
    
    implementation(libs.nordic.blek.client)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.hilt.navigation.compose)
}
