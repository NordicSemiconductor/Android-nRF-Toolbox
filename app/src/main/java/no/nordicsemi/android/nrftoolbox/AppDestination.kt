package no.nordicsemi.android.nrftoolbox

import no.nordicsemi.android.bps.repository.BPS_SERVICE_UUID
import no.nordicsemi.android.bps.view.BPSScreen
import no.nordicsemi.android.cgms.repository.CGMS_SERVICE_UUID
import no.nordicsemi.android.cgms.view.CGMScreen
import no.nordicsemi.android.csc.repository.CSC_SERVICE_UUID
import no.nordicsemi.android.csc.view.CSCScreen
import no.nordicsemi.android.gls.repository.GLS_SERVICE_UUID
import no.nordicsemi.android.gls.view.GLSScreen
import no.nordicsemi.android.hrs.service.HRS_SERVICE_UUID
import no.nordicsemi.android.hrs.view.HRSScreen
import no.nordicsemi.android.hts.repository.HTS_SERVICE_UUID
import no.nordicsemi.android.hts.view.HTSScreen
import no.nordicsemi.android.navigation.ComposeDestination
import no.nordicsemi.android.navigation.ComposeDestinations
import no.nordicsemi.android.prx.service.PRX_SERVICE_UUID
import no.nordicsemi.android.prx.view.PRXScreen
import no.nordicsemi.android.rscs.service.RSCS_SERVICE_UUID
import no.nordicsemi.android.rscs.view.RSCSScreen
import no.nordicsemi.android.uart.repository.UART_SERVICE_UUID
import no.nordicsemi.android.uart.view.UARTScreen
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceScreen
import java.util.*

val HomeDestinations = ComposeDestinations(HomeDestination.values().map { it.destination })
val ProfileDestinations = ComposeDestinations(ProfileDestination.values().map { it.destination })

enum class HomeDestination(val destination: ComposeDestination) {
    HOME(ComposeDestination("home-destination") { HomeScreen() }),
    SCANNER(ComposeDestination("scanner-destination") { FindDeviceScreen() });
}

enum class ProfileDestination(val destination: ComposeDestination, val uuid: UUID) {
    CSC(ComposeDestination("csc-destination") { CSCScreen() }, CSC_SERVICE_UUID),
    HRS(ComposeDestination("hrs-destination") { HRSScreen() }, HRS_SERVICE_UUID),
    HTS(ComposeDestination("hts-destination") { HTSScreen() }, HTS_SERVICE_UUID),
    GLS(ComposeDestination("gls-destination") { GLSScreen() }, GLS_SERVICE_UUID),
    BPS(ComposeDestination("bps-destination") { BPSScreen() }, BPS_SERVICE_UUID),
    PRX(ComposeDestination("prx-destination") { PRXScreen() }, PRX_SERVICE_UUID),
    RSCS(ComposeDestination("rscs-destination") { RSCSScreen() }, RSCS_SERVICE_UUID),
    CGMS(ComposeDestination("cgms-destination") { CGMScreen() }, CGMS_SERVICE_UUID),
    UART(ComposeDestination("uart-destination") { UARTScreen() }, UART_SERVICE_UUID);
}
