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
import no.nordicsemi.android.ui.view.MockRemoteService
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
    val device: MockRemoteService
        get() = MockRemoteService(
            serviceData = remoteService,
            peripheral = peripheral
        )

    private val _stopEvent = simpleSharedFlow<DisconnectAndStopEvent>()
    internal val stopEvent = _stopEvent.asSharedFlow()

    val isRunning = data.map { it.connectionState == ConnectionState.Connected }
    private var isOnScreen = false
    private var isServiceRunning = false

    fun setOnScreen(isOnScreen: Boolean) {
        this.isOnScreen = isOnScreen

        if (shouldClean()) clean()
    }

    fun getConnection(scope: CoroutineScope) {
        peripheral?.state?.onEach {
            _data.value = _data.value.copy(
                connectionState = it,
            )
        }
            ?.launchIn(scope)
    }

    fun disconnect() {
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    fun setServiceRunning(serviceRunning: Boolean) {
        this.isServiceRunning = serviceRunning

        if (shouldClean()) clean()
    }


    fun onHTSDataChanged(data: HtsData) {
        _data.value = _data.value.copy(data = data)
    }

    fun onBatteryLevelChanged(batteryLevel: Int) {
        _data.value = _data.value.copy(batteryLevel = batteryLevel)
    }

    fun launch(device: MockRemoteService) {
        _data.value = _data.value.copy(deviceName = device.peripheral?.name)
        serviceManager.startService(HTSService::class.java)
    }

    private fun shouldClean() = !isOnScreen && !isServiceRunning

    private fun clean() {
        // logger = null
        _data.value = HTSServiceData()
    }

    fun onTemperatureUnitChanged(temperatureUnit: TemperatureUnit) {
        _data.value = _data.value.copy(
            temperatureUnit = temperatureUnit
        )
    }
}