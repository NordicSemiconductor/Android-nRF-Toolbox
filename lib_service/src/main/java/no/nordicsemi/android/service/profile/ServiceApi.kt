package no.nordicsemi.android.service.profile

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.service.services.ServiceManager
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.WriteType

/** Device disconnection reason. */
sealed interface DeviceDisconnectionReason

/** Includes the [ConnectionState.Disconnected.Reason]. */
data class StateReason(val reason: ConnectionState.Disconnected.Reason) : DeviceDisconnectionReason

/** Includes the custom made [DisconnectReason] to include other disconnection reasons which are not included in the [ConnectionState.Disconnected.Reason]. */
data class CustomReason(val reason: DisconnectReason) :
    DeviceDisconnectionReason

interface ServiceApi {

    /** Flow of connected devices. */
    val connectedDevices: Flow<Map<String, Pair<Peripheral, List<ServiceManager>>>>

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

    /**
     * Request maximum write value length.
     * For [WriteType.WITHOUT_RESPONSE] it is equal to *ATT MTU - 3 bytes*.
     */
    suspend fun getMaxWriteValue(
        address: String,
        writeType: WriteType = WriteType.WITHOUT_RESPONSE
    ): Int?

    suspend fun createBonding(
        address: String
    )

}
