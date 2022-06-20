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

package no.nordicsemi.android.nrftoolbox

import no.nordicsemi.android.bps.view.BPSScreen
import no.nordicsemi.android.cgms.view.CGMScreen
import no.nordicsemi.android.csc.view.CSCScreen
import no.nordicsemi.android.gls.main.view.GLSScreen
import no.nordicsemi.android.hrs.view.HRSScreen
import no.nordicsemi.android.hts.view.HTSScreen
import no.nordicsemi.android.navigation.ComposeDestination
import no.nordicsemi.android.navigation.ComposeDestinations
import no.nordicsemi.android.nrftoolbox.view.HomeScreen
import no.nordicsemi.android.prx.view.PRXScreen
import no.nordicsemi.android.rscs.view.RSCSScreen
import no.nordicsemi.android.uart.view.UARTScreen
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceScreen

val HomeDestinations = ComposeDestinations(HomeDestination.values().map { it.destination })
val ProfileDestinations = ComposeDestinations(ProfileDestination.values().map { it.destination })

enum class HomeDestination(val destination: ComposeDestination) {
    HOME(ComposeDestination("home-destination") { HomeScreen() }),
    SCANNER(ComposeDestination("scanner-destination") { FindDeviceScreen() });
}

enum class ProfileDestination(val destination: ComposeDestination) {
    CSC(ComposeDestination("csc-destination") { CSCScreen() }),
    HRS(ComposeDestination("hrs-destination") { HRSScreen() }),
    HTS(ComposeDestination("hts-destination") { HTSScreen() }),
    GLS(ComposeDestination("gls-destination") { GLSScreen() }),
    BPS(ComposeDestination("bps-destination") { BPSScreen() }),
    PRX(ComposeDestination("prx-destination") { PRXScreen() }),
    RSCS(ComposeDestination("rscs-destination") { RSCSScreen() }),
    CGMS(ComposeDestination("cgms-destination") { CGMScreen() }),
    UART(ComposeDestination("uart-destination") { UARTScreen() });
}
