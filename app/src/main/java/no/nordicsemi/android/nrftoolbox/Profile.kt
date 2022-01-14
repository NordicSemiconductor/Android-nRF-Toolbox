package no.nordicsemi.android.nrftoolbox

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
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

enum class NavigationId(val id: String) {
    HOME("home-screen"),
    SCANNER("scanner-screen"),
    CSC("csc-screen"),
    HRS("hrs-screen"),
    HTS("hts-screen"),
    GLS("gls-screen"),
    BPS("bps-screen"),
    PRX("prx-screen"),
    RSCS("rscs-screen"),
    CGMS("cgms-screen"),
    UART("uart-screen");
}

@Parcelize
enum class Profile(val uuid: UUID, val isPairingRequired: Boolean) : Parcelable {
    CSC(CYCLING_SPEED_AND_CADENCE_SERVICE_UUID, false),
    HRS(HR_SERVICE_UUID, false),
    HTS(HT_SERVICE_UUID, false),
    GLS(GLS_SERVICE_UUID, true),
    BPS(BPS_SERVICE_UUID, false),
    PRX(PRX_SERVICE_UUID, true),
    RSCS(RSCS_SERVICE_UUID, false),
    CGMS(CGMS_SERVICE_UUID, false),
    UART(UART_SERVICE_UUID, false);
}

fun Profile.toNavigationId(): NavigationId {
    return when (this) {
        Profile.CSC -> NavigationId.CSC
        Profile.HRS -> NavigationId.HRS
        Profile.HTS -> NavigationId.HTS
        Profile.GLS -> NavigationId.GLS
        Profile.BPS -> NavigationId.BPS
        Profile.PRX -> NavigationId.PRX
        Profile.RSCS -> NavigationId.RSCS
        Profile.CGMS -> NavigationId.CGMS
        Profile.UART -> NavigationId.UART
    }
}
