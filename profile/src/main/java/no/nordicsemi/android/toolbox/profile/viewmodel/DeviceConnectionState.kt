package no.nordicsemi.android.toolbox.profile.viewmodel

import no.nordicsemi.android.service.profile.DeviceDisconnectionReason
import no.nordicsemi.android.service.services.ServiceManager
import no.nordicsemi.android.toolbox.profile.data.ProfileServiceData
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState

internal data class DeviceData(
    val peripheral: Peripheral? = null,
    val peripheralProfileMap: Map<Peripheral, List<ServiceManager>> = emptyMap(),
    val connectionState: ConnectionState? = null,
    val serviceData: List<ProfileServiceData> = emptyList(),
    val isMissingServices: Boolean = false,
    val maxValueLength: Int? = null,
)

internal sealed class DeviceConnectionState {
    data object Idle : DeviceConnectionState()
    data object Connecting : DeviceConnectionState()
    data object Disconnecting : DeviceConnectionState()
    data class Connected(val data: DeviceData) : DeviceConnectionState()
    data class Disconnected(
        val device: Peripheral? = null,
        val reason: DeviceDisconnectionReason?
    ) : DeviceConnectionState()
}
