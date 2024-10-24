package no.nordicsemi.android.toolbox.libs.profile.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor() {
    private val _connectedDevices = MutableStateFlow<Map<String, Pair<Peripheral, List<ProfileHandler>>>>(emptyMap())
    val connectedDevices = _connectedDevices.asStateFlow()

    fun updateConnectedDevices(devices: Map<String, Pair<Peripheral, List<ProfileHandler>>>) {
        _connectedDevices.value = devices
    }
}
