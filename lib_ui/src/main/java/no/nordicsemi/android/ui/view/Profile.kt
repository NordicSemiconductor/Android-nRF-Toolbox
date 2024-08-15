package no.nordicsemi.android.ui.view

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.Peripheral

@Parcelize
data class MockRemoteService(
    val serviceData: @RawValue RemoteService? = null,
    val peripheral: @RawValue Peripheral? = null,
) : Parcelable

@Parcelize
sealed interface Profile : Parcelable {
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
