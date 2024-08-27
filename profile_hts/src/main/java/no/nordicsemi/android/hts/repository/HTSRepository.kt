package no.nordicsemi.android.hts.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.core.simpleSharedFlow
import no.nordicsemi.android.hts.data.HTSServiceData
import no.nordicsemi.android.hts.data.HtsData
import no.nordicsemi.android.hts.view.TemperatureUnit
import no.nordicsemi.android.service.DisconnectAndStopEvent
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.toolbox.libs.profile.PeripheralDetails
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HTSRepository @Inject constructor(
    private val serviceManager: ServiceManager,
) {
    private val _data = MutableStateFlow(HTSServiceData())
    val data = _data.asStateFlow()
    var peripheral: Peripheral? = null
    var remoteService: RemoteService? = null
    val device: PeripheralDetails
        get() = PeripheralDetails(
            serviceData = remoteService,
            peripheral = peripheral
        )

    private val _stopEvent = simpleSharedFlow<DisconnectAndStopEvent>()
    internal val stopEvent = _stopEvent.asSharedFlow()

    val isRunning = data.map { it.connectionState == ConnectionState.Connected }

    fun getConnection(scope: CoroutineScope) {
        peripheral?.state?.onEach {
            _data.value = _data.value.copy(
                connectionState = it,
            )
        }?.launchIn(scope)
    }

    fun disconnect() {
        _data.value = _data.value.copy(
            connectionState = ConnectionState.Disconnected(),
        )
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    fun onHTSDataChanged(data: HtsData) {
        _data.value = _data.value.copy(data = data)
    }

    fun onBatteryLevelChanged(batteryLevel: Int) {
        _data.value = _data.value.copy(batteryLevel = batteryLevel)
    }

    fun launch() {
        _data.value = _data.value.copy(deviceName = peripheral?.name)
        serviceManager.startService(HTSService::class.java)
    }

    fun onTemperatureUnitChanged(temperatureUnit: TemperatureUnit) {
        _data.value = _data.value.copy(
            temperatureUnit = temperatureUnit
        )
    }
}