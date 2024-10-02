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
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.HTSServiceData
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

data class DeviceData(
    val peripheral: Peripheral? = null,
    val connectionState: ConnectionState? = null,
    val htsServiceData: HTSServiceData = HTSServiceData(),
    val hrsServiceData: HRSServiceData = HRSServiceData(),
    val batteryLevel: Int? = null,
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
                    // Update the battery level, if any.
                    api.batteryLevel.onEach {
                        _deviceData.value = _deviceData.value.copy(
                            batteryLevel = it,
                        )
                    }.launchIn(viewModelScope)
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
                profileHandler.getNotification().onEach {
                    _deviceData.value = _deviceData.value.copy(
                        htsServiceData = _deviceData.value.htsServiceData.copy(
                            data = it as HtsData,
                        )
                    )
                }.launchIn(viewModelScope)
            }
            // Handle the HRS profile data
            Profile.HRS -> {
                profileHandler.getNotification().onEach {
                    _deviceData.value = _deviceData.value.copy(
                        hrsServiceData = _deviceData.value.hrsServiceData.copy(
                            data = _deviceData.value.hrsServiceData.data + it as HRSData,
                        )
                    )
                }.launchIn(viewModelScope)
                profileHandler.readCharacteristic()?.let {
                    _deviceData.value = _deviceData.value.copy(
                        hrsServiceData = _deviceData.value.hrsServiceData.copy(
                            bodySensorLocation = it as Int,
                        )
                    )
                }
            }
            // TODO: Add more profile modules here
            else -> TODO()
        }
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
            htsServiceData = _deviceData.value.htsServiceData.copy(
                temperatureUnit = unit
            )
        )
    }

    /** Switch the zoom event. */
    private fun switchZoomEvent() {
        _deviceData.value = _deviceData.value.copy(
            hrsServiceData = _deviceData.value.hrsServiceData.copy(
                zoomIn = !_deviceData.value.hrsServiceData.zoomIn
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }

}
