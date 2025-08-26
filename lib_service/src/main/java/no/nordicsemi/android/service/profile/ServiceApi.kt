package no.nordicsemi.android.service.profile

import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.toolbox.profile.manager.ServiceManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.WriteType

/**
 * Represents the public-facing API for the ProfileService.
 */
interface ServiceApi {

    /** A data class to hold all relevant information about a connected device. */
    data class DeviceData(
        val peripheral: Peripheral,
        val connectionState: ConnectionState = ConnectionState.Connecting,
        val services: List<ServiceManager> = emptyList()
    )

    /** A data class to represent a disconnection event. */
    data class DisconnectionEvent(val address: String, val reason: DeviceDisconnectionReason)

    /**
     * A flow that emits the current state of all managed devices.
     * The map key is the device address.
     */
    val devices: StateFlow<Map<String, DeviceData>>

    /**
     * A flow that emits whether a specific device is missing its required services.
     * The map key is the device address.
     */
    val isMissingServices: StateFlow<Map<String, Boolean>>

    /**
     * A flow that emits the reason for the last disconnection event for any device.
     */
    val disconnectionEvent: StateFlow<DisconnectionEvent?>

    /**
     * Disconnects from a Bluetooth device and stops managing it.
     *
     * @param address The address of the device to disconnect from.
     */
    fun disconnect(address: String)

    /**
     * Retrieves a peripheral instance by its address.
     *
     * @param address The device address.
     * @return The [Peripheral] instance, or null if not found.
     */
    fun getPeripheral(address: String?): Peripheral?

    /**
     * Requests the maximum possible value length for a write operation.
     *
     * @param address The device address.
     * @param writeType The type of write operation.
     * @return The maximum number of bytes that can be sent in a single write.
     */
    suspend fun getMaxWriteValue(
        address: String,
        writeType: WriteType = WriteType.WITHOUT_RESPONSE
    ): Int?

    /**
     * Initiates and waits for the bonding process to complete with a device.
     *
     * @param address The device address.
     */
    suspend fun createBond(address: String)
}