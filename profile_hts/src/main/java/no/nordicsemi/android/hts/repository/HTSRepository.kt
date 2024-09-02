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

    private var isOnScreen = false
    private var isServiceRunning = false

    /** Sets the value of the [isOnScreen] flag. */
    fun setOnScreen(isOnScreen: Boolean) {
        this.isOnScreen = isOnScreen

        if (shouldClean()) clean()
    }

    /** Sets the value of the [isServiceRunning] flag. */
    fun setServiceRunning(serviceRunning: Boolean) {
        this.isServiceRunning = serviceRunning
        _data.value = _data.value.copy(isServiceRunning = serviceRunning)

        if (shouldClean()) clean()
    }

    /** Cleans the repository if the service is not running and the screen is off. */
    private fun shouldClean() = !isOnScreen && !isServiceRunning

    /** Launches the HTS service. */
    fun launchHtsService() {
        _data.value = _data.value.copy(deviceName = peripheral?.name)
        serviceManager.startService(HTSService::class.java)
    }

    /** Gets the connection state of the peripheral device. */
    fun getConnection(scope: CoroutineScope) {
        peripheral?.state?.onEach {
            _data.value = _data.value.copy(
                connectionState = it,
            )
        }?.launchIn(scope)
    }

    /** Disconnects the device and stops the service. */
    fun disconnect() {
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    /** Collects the HTS data from the characteristic and updates the UI. */
    fun onHTSDataChanged(data: HtsData) {
        _data.value = _data.value.copy(data = data)
    }

    /** Collects the battery level from the characteristic and updates the UI. */
    fun onBatteryLevelChanged(batteryLevel: Int) {
        _data.value = _data.value.copy(batteryLevel = batteryLevel)
    }

    /** Collects the temperature unit from the characteristic and updates the UI. */
    fun onTemperatureUnitChanged(temperatureUnit: TemperatureUnit) {
        _data.value = _data.value.copy(
            temperatureUnit = temperatureUnit
        )
    }

    /** Cleans the repository. */
    private fun clean() {
        _data.value = HTSServiceData()
    }

}