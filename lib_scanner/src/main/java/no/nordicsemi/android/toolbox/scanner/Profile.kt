package no.nordicsemi.android.toolbox.scanner

import android.os.Parcelable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState

@Parcelize
data class MockRemoteService(
    val serviceData: @RawValue RemoteService? = null,
    val connectionState: @RawValue StateFlow<ConnectionState>? = null,
    val peripheral: @RawValue Peripheral?=null,
) : Parcelable

@Parcelize
sealed interface Profile: Parcelable {
    val remoteService: MockRemoteService
        get() = MockRemoteService()

    @Parcelize
    data class HTS(
        override val remoteService: MockRemoteService = MockRemoteService()
    ) : Profile

    @Parcelize
    data class HRS(
        override val remoteService: MockRemoteService = MockRemoteService()
    ) : Profile

    @Parcelize
    data class BPS(
        override val remoteService: MockRemoteService = MockRemoteService()
    ) : Profile
}
