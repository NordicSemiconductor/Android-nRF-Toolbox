package no.nordicsemi.android.toolbox.libs.profile.service

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState

interface ServiceApi {

    /** Flow of connected devices. */
    val connectedDevices: Flow<Map<String, Pair<Peripheral, List<ProfileHandler>>>>

    /** Missing services flag. */
    val isMissingServices: Flow<Boolean>

    /**
     * Get the peripheral by its address.
     * @param address the device address.
     * @return the peripheral instance.
     */
    fun getPeripheralById(address: String?): Peripheral?

    /**
     * Disconnect the device with the given address.
     *
     * @param deviceAddress the device address.
     */
    fun disconnect(deviceAddress: String)

    /**
     * Get the connection state of the device with the given address.
     *
     * @return the connection state flow.
     */
    fun getConnectionState(address: String): Flow<ConnectionState>?
}
