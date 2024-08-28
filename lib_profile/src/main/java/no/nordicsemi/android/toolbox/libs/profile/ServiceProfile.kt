package no.nordicsemi.android.toolbox.libs.profile

import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.Peripheral

data class PeripheralDetails(
    val peripheral: Peripheral? = null,
    val serviceData: RemoteService? = null,
)

sealed interface ServiceProfile {
    val peripheralDetails: PeripheralDetails
        get() = PeripheralDetails()


    data class HTS(
        override val peripheralDetails: PeripheralDetails = PeripheralDetails()
    ) : ServiceProfile

    data class HRS(
        override val peripheralDetails: PeripheralDetails = PeripheralDetails()
    ) : ServiceProfile

    data class CGMS(
        override val peripheralDetails: PeripheralDetails = PeripheralDetails()
    ) : ServiceProfile

    data class BPS(
        override val peripheralDetails: PeripheralDetails = PeripheralDetails()
    ) : ServiceProfile


    data class CSC(
        override val peripheralDetails: PeripheralDetails = PeripheralDetails()
    ) : ServiceProfile


    data class GLS(
        override val peripheralDetails: PeripheralDetails = PeripheralDetails()
    ) : ServiceProfile

    data class PRX(
        override val peripheralDetails: PeripheralDetails = PeripheralDetails()
    ) : ServiceProfile


    data class RSCS(
        override val peripheralDetails: PeripheralDetails = PeripheralDetails()
    ) : ServiceProfile

    data class UART(
        override val peripheralDetails: PeripheralDetails = PeripheralDetails()
    ) : ServiceProfile
}
