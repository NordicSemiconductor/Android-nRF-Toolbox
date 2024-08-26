package no.nordicsemi.android.toolbox.libs.profile

import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.Peripheral

data class PeripheralDetails(
    val peripheral: Peripheral? = null,
    val serviceData: RemoteService? = null,
)

sealed interface Profile {
    val remoteService: PeripheralDetails
        get() = PeripheralDetails()


    data class HTS(
        override val remoteService: PeripheralDetails = PeripheralDetails()
    ) : Profile

    data class HRS(
        override val remoteService: PeripheralDetails = PeripheralDetails()
    ) : Profile

    data class CGMS(
        override val remoteService: PeripheralDetails = PeripheralDetails()
    ) : Profile

    data class BPS(
        override val remoteService: PeripheralDetails = PeripheralDetails()
    ) : Profile


    data class CSC(
        override val remoteService: PeripheralDetails = PeripheralDetails()
    ) : Profile


    data class GLS(
        override val remoteService: PeripheralDetails = PeripheralDetails()
    ) : Profile

    data class PRX(
        override val remoteService: PeripheralDetails = PeripheralDetails()
    ) : Profile


    data class RSCS(
        override val remoteService: PeripheralDetails = PeripheralDetails()
    ) : Profile

    data class UART(
        override val remoteService: PeripheralDetails = PeripheralDetails()
    ) : Profile
}
