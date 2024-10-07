package no.nordicsemi.android.toolbox.libs.profile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.logger.LoggerLauncher
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.timber.nRFLoggerTree
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.HtsData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.TemperatureUnit
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.repository.DeviceRepository
import no.nordicsemi.android.toolbox.libs.profile.service.ServiceApi
import no.nordicsemi.android.toolbox.libs.profile.service.ServiceManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import java.lang.ref.WeakReference
import javax.inject.Inject

sealed class ProfileServiceData

/**
 * HTS service data class that holds the HTS data.
 *
 * @param data The HTS data.
 * @param temperatureUnit The temperature unit.
 */
data class HTSServiceData(
    val data: HtsData = HtsData(),
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
) : ProfileServiceData()

/**
 * Heart Rate Service data.
 * @param data the list of heart rate data.
 * @param bodySensorLocation the body sensor location.
 * @param zoomIn true if the chart is zoomed in.
 */
data class HRSServiceData(
    val data: List<HRSData> = emptyList(),
    val bodySensorLocation: Int? = null,
    val zoomIn: Boolean = false,
) : ProfileServiceData() {
    val heartRates = data.map { it.heartRate }
}

data class BatteryServiceData(
    val batteryLevel: Int? = null,
) : ProfileServiceData()

data class DeviceData(
    val peripheral: Peripheral? = null,
    val connectionState: ConnectionState? = null,
    val serviceData: List<ProfileServiceData> = emptyList(),
    val isMissingServices: Boolean = false,
)

@HiltViewModel
open class DeviceConnectionViewModel @Inject constructor(
    private val serviceManager: ServiceManager,
    private val navigator: Navigator,
    private val deviceRepository: DeviceRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private var address: String? = null
    private val _deviceData = MutableStateFlow(DeviceData())

    private var logger: nRFLoggerTree? = null

    val deviceData = _deviceData.asStateFlow()
    private var serviceApi: WeakReference<ServiceApi>? = null

    /**
     * Bind the service and return the API if successful.
     */
    private suspend fun getServiceApi(): ServiceApi? {
        if (serviceApi == null) {
            serviceApi = WeakReference(serviceManager.bindService())
        }
        return serviceApi?.get()
    }

    init {
        viewModelScope.launch {
            getServiceApi()?.connectedDevices?.onEach { peripheralProfileMap ->
                deviceRepository.updateConnectedDevices(peripheralProfileMap)
            }?.launchIn(viewModelScope)

        }
    }

    /**
     * Unbind the service.
     */
    fun unbindService() {
        serviceApi?.let { serviceManager.unbindService() }
        serviceApi = null
    }

    /**
     * Handle click events from the view.
     */
    fun onClickEvent(event: DeviceConnectionViewEvent) {
        // Handle click events
        when (event) {
            is DisconnectEvent -> disconnectAndNavigate(event.device)
            NavigateUp -> disconnectIfNeededAndNavigate()
            is OnRetryClicked -> reConnectDevice(event.device)
            is OnTemperatureUnitSelected -> updateTemperatureUnit(event.value)
            OpenLoggerEvent -> openLogger()
            SwitchZoomEvent -> switchZoomEvent()
        }

    }

    /**
     * Connect to the peripheral with the given address. Before connecting, the service must be bound.
     * The service will be started if not already running.
     * @param deviceAddress the address of the peripheral to connect to.
     */
    fun connectToPeripheral(deviceAddress: String) = viewModelScope.launch {
        // Update the device address
        address = deviceAddress
        // Connect to the peripheral
        getServiceApi()?.let { api ->
            val peripheral = api.getPeripheralById(deviceAddress)
            if (peripheral?.isConnected != true) {
                serviceManager.connectToPeripheral(deviceAddress)
            }
            updateServiceData(api, deviceAddress)
        }
    }

    /**
     * Update the service data, including connection state and peripheral data.
     * @param api the service API.
     * @param deviceAddress the address of the connected device.
     */
    private fun updateServiceData(api: ServiceApi, deviceAddress: String) {
        // Observe the handlers for the connected device
        api.getConnectionState(deviceAddress)?.onEach { connectionState ->
            _deviceData.value = _deviceData.value.copy(
                connectionState = connectionState,
            )
            if (connectionState == ConnectionState.Connected) {
                val peripheral = api.getPeripheralById(deviceAddress)
                _deviceData.value = _deviceData.value.copy(
                    peripheral = peripheral,
                )
                api.isMissingServices.onEach { isMissing ->
                    _deviceData.value = _deviceData.value.copy(
                        isMissingServices = isMissing,
                    )
                    if (!isMissing) {
                        // Observe the data from the connected device
                        updateConnectedData(api, peripheral)
                    }
                }.launchIn(viewModelScope)
            } else if (connectionState is ConnectionState.Disconnected) {
                // unbind the service
                unbindService()
            }
        }?.launchIn(viewModelScope)
    }

    /**
     * Update the connected data, including the peripheral, profile data, and battery level.
     * @param api the service API.
     * @param peripheral the address of the connected device.
     */
    private fun updateConnectedData(
        api: ServiceApi,
        peripheral: Peripheral?
    ) {
        api.connectedDevices.onEach { peripheralProfileMap ->
            peripheral?.let { device ->
                // Update the profile data
                peripheralProfileMap[device]?.forEach { profileHandler ->
                    updateProfileData(profileHandler)
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Observe and update the data from the profile handler.
     * @param profileHandler the profile handler.
     */
    private fun updateProfileData(profileHandler: ProfileHandler) {
        when (profileHandler.profile) {
            Profile.HTS -> {
                profileHandler.getNotification().onEach { notificationData ->
                    val htsData = notificationData as HtsData
                    _deviceData.updateOrAddDataFlow(
                        HTSServiceData(data = htsData)
                    ) { existingServiceData ->
                        existingServiceData.copy(data = htsData)
                    }
                }.launchIn(viewModelScope)
            }

            Profile.HRS -> {
                profileHandler.getNotification().onEach { notificationData ->
                    val hrsData = notificationData as HRSData
                    _deviceData.updateOrAddDataFlow(
                        HRSServiceData(data = listOf(hrsData))
                    ) { existingServiceData ->
                        existingServiceData.copy(data = existingServiceData.data + hrsData)
                    }
                }.launchIn(viewModelScope)

                profileHandler.readCharacteristic()?.onEach {
                    val characteristicData = it as Int
                    _deviceData.updateOrAddDataFlow(
                        HRSServiceData(bodySensorLocation = characteristicData)
                    ) { existingServiceData ->
                        existingServiceData.copy(bodySensorLocation = characteristicData)
                    }
                }?.launchIn(viewModelScope)
            }

            Profile.BATTERY -> {
                profileHandler.getNotification().onEach { notificationData ->
                    val batteryLevel = notificationData as Int
                    _deviceData.updateOrAddDataFlow(
                        BatteryServiceData(batteryLevel = batteryLevel)
                    ) { existingServiceData ->
                        existingServiceData.copy(batteryLevel = batteryLevel)
                    }
                }.launchIn(viewModelScope)
            }

            // TODO: Add more profile modules here
            else -> TODO()
        }
    }

    /**
     * Update or add the profile service notification data.
     * @param newData the new data to update or add.
     * @param update the update function.
     */
    private inline fun <reified T : ProfileServiceData> MutableStateFlow<DeviceData>.updateOrAddDataFlow(
        newData: T,
        crossinline update: (T) -> T
    ) {
        value = if (value.serviceData.any { it is T }) {
            value.copy(
                serviceData = value.serviceData.map {
                    if (it is T) update(it) else it
                }
            )
        } else {
            value.copy(
                serviceData = value.serviceData + newData
            )
        }
    }

    /**
     * Launch the logger activity.
     */
    private fun openLogger() {
        LoggerLauncher.launch(context, logger?.session as? LogSession)
    }

    /**
     * Reconnect to the device with the given address.
     * @param address the address of the device to reconnect to.
     */
    private fun reConnectDevice(address: String) = viewModelScope.launch {
        // Clear the flags and state.
        _deviceData.value = DeviceData()
        getServiceApi()?.let {
            connectToPeripheral(address)
        }
    }

    /**
     * Disconnect the device if missing services and navigate back.
     */
    private fun disconnectIfNeededAndNavigate() = viewModelScope.launch {
        // Disconnect the peripheral if missing services.
        if (_deviceData.value.isMissingServices) {
            getServiceApi()?.apply {
                address?.let { disconnect(it) }
            }
        }
        // navigate back
        navigator.navigateUp()
    }

    /**
     * Disconnect the device with the given address and navigate back.
     * @param device the address of the device to disconnect.
     */
    private fun disconnectAndNavigate(device: String) = viewModelScope.launch {
        getServiceApi()?.apply {
            disconnect(device)
            // Unbind the service.
            unbindService()
        }
        // navigate back
        navigator.navigateUp()
    }

    /**
     * Update the temperature unit.
     * @param unit the temperature unit.
     */
    private fun updateTemperatureUnit(unit: TemperatureUnit) {
        // Handle temperature unit selection
        _deviceData.value = _deviceData.value.copy(
            serviceData = _deviceData.value.serviceData.map { service ->
                if (service is HTSServiceData) {
                    service.copy(temperatureUnit = unit)
                } else {
                    service
                }
            }
        )
    }

    /** Switch the zoom event. */
    private fun switchZoomEvent() {
        _deviceData.value = _deviceData.value.copy(
            serviceData = _deviceData.value.serviceData.map { service ->
                if (service is HRSServiceData) {
                    service.copy(zoomIn = !service.zoomIn)
                } else {
                    service
                }
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }

}
