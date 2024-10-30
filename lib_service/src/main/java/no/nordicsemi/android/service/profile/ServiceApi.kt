package no.nordicsemi.android.service.profile

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.service.handler.ProfileHandler
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState

/** Device disconnection reason. */
sealed interface DeviceDisconnectionReason

/** Includes the [ConnectionState.Disconnected.Reason]. */
data class StateReason(val reason: ConnectionState.Disconnected.Reason) : DeviceDisconnectionReason

/** Includes the custom made [DisconnectReason] to include other disconnection reasons which are not included in the [ConnectionState.Disconnected.Reason]. */
data class CustomReason(val reason: DisconnectReason) :
    DeviceDisconnectionReason

interface ServiceApi {

    /** Flow of connected devices. */
    val connectedDevices: Flow<Map<String, Pair<Peripheral, List<ProfileHandler>>>>

    /** Missing services flag. */
    val isMissingServices: Flow<Boolean>

    /**
     * Get the peripheral by its [address].
     *
     * @return the peripheral instance.
     */
    fun getPeripheralById(address: String?): Peripheral?

    /**
     * Disconnect the device with the given [deviceAddress].
     *
     * @param deviceAddress the device address.
     */
    fun disconnect(deviceAddress: String)

    /**
     * Get the connection state of the device with the given [address].
     *
     * @return the connection state flow.
     */
    fun getConnectionState(address: String): Flow<ConnectionState>?

    /**
     * Get the disconnection reason of the device with the given address.
     *
     * @return the disconnection reason flow.
     */
    val disconnectionReason: Flow<DeviceDisconnectionReason?>
}
