package no.nordicsemi.android.nrftoolbox

import no.nordicsemi.android.bps.repository.BPS_SERVICE_UUID
import no.nordicsemi.android.cgms.repository.CGMS_SERVICE_UUID
import no.nordicsemi.android.csc.repository.CYCLING_SPEED_AND_CADENCE_SERVICE_UUID
import no.nordicsemi.android.gls.repository.GLS_SERVICE_UUID
import no.nordicsemi.android.hrs.service.HR_SERVICE_UUID
import no.nordicsemi.android.hts.repository.HT_SERVICE_UUID
import no.nordicsemi.android.prx.service.PRX_SERVICE_UUID
import no.nordicsemi.android.rscs.service.RSCS_SERVICE_UUID
import no.nordicsemi.android.uart.repository.UART_SERVICE_UUID
import java.util.*

const val ARGS_KEY = "args"

enum class NavDestination(val id: String, val uuid: UUID?, val pairingRequired: Boolean) {
    HOME("home-screen", null, false),
    CSC("csc-screen", CYCLING_SPEED_AND_CADENCE_SERVICE_UUID, false),
    HRS("hrs-screen", HR_SERVICE_UUID, false),
    HTS("hts-screen", HT_SERVICE_UUID, false),
    GLS("gls-screen", GLS_SERVICE_UUID, true),
    BPS("bps-screen", BPS_SERVICE_UUID, false),
    PRX("prx-screen", PRX_SERVICE_UUID, true),
    RSCS("rscs-screen", RSCS_SERVICE_UUID, false),
    CGMS("cgms-screen", CGMS_SERVICE_UUID, false),
    UART("uart-screen", UART_SERVICE_UUID, false),
    DFU("dfu-screen", null, false); //todo check characteristic
}
