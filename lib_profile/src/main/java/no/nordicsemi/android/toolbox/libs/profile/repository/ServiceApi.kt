package no.nordicsemi.android.toolbox.libs.profile.repository

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState

interface ServiceApi {
    val connectedDevices: Flow<Map<Peripheral, List<ProfileHandler>>>
    val isMissingServices: Flow<Boolean>
    val batteryLevel: Flow<Int?>
    fun getPeripheralById(address: String?): Peripheral?
    fun disconnect(deviceAddress: String)
    fun getConnectionState(address: String): Flow<ConnectionState>?
}
