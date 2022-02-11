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
