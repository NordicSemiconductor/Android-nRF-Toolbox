package no.nordicsemi.android.toolbox.libs.profile.viewmodel

import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.logger.LoggerLauncher
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.timber.nRFLoggerTree
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.bps.BPSData
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.HtsData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.TemperatureUnit
import no.nordicsemi.android.toolbox.libs.profile.data.service.BPSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.BatteryServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.HRSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.ProfileServiceData
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
    val serviceData: List<ProfileServiceData> = emptyList(),
    val isMissingServices: Boolean = false,
    val disconnectionReason: ConnectionState.Disconnected.Reason? = null,
)

sealed class DeviceConnectionState {
    data object Idle : DeviceConnectionState()
    data object Connecting : DeviceConnectionState()
    data class Connected(val data: DeviceData) : DeviceConnectionState()
    data class Disconnected(val reason: ConnectionState.Disconnected.Reason?) :
        DeviceConnectionState()

    data class Error(val message: String) : DeviceConnectionState()
}

@HiltViewModel
open class DeviceConnectionViewModel @Inject constructor(
    private val serviceManager: ServiceManager,
    private val navigator: Navigator,
    private val deviceRepository: DeviceRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private var address: String? = null
    private val _deviceData = MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.Idle)
    val deviceData = _deviceData.asStateFlow()

    private var logger: nRFLoggerTree? = null
    private var serviceApi: WeakReference<ServiceApi>? = null

    private var job: Job? = null
    private var connectedDeviceJob: Job? = null

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
        observeConnectedDevices()
    }

    /**
     * Observe connected devices and update the repository.
     */
    private fun observeConnectedDevices() = viewModelScope.launch {
        getServiceApi()?.connectedDevices?.onEach { peripheralProfileMap ->
            deviceRepository.updateConnectedDevices(peripheralProfileMap)
        }?.launchIn(viewModelScope)
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
            updateConnectionState(api, deviceAddress, peripheral?.isConnected == true)
        }
    }

    /**
     * Update the service data, including connection state and peripheral data.
     * @param api the service API.
     * @param deviceAddress the address of the connected device.
     */
    private fun updateConnectionState(
        api: ServiceApi,
        deviceAddress: String,
        isAlreadyConnected: Boolean
    ) {
        val disconnectionReason = mutableStateOf<ConnectionState.Disconnected.Reason?>(null)
        // Drop the first default state (Closed) before connection.
        job = api.getConnectionState(deviceAddress)
            ?.drop(if (isAlreadyConnected) 0 else 1)
            ?.onEach { connectionState ->
                when (connectionState) {
                    ConnectionState.Connected -> {
                        val peripheral = api.getPeripheralById(deviceAddress)
                        _deviceData.update {
                            DeviceConnectionState.Connected(
                                DeviceData(
                                    peripheral = peripheral
                                )
                            )
                        }.apply { checkForMissingServices(api, peripheral) }
                    }

                    is ConnectionState.Disconnected -> {
                        disconnectionReason.value = connectionState.reason
                        _deviceData.update {
                            DeviceConnectionState.Disconnected(connectionState.reason)
                        }
                    }

                    ConnectionState.Closed -> {
                        unbindService()
                        _deviceData.update {
                            DeviceConnectionState.Disconnected(disconnectionReason.value)
                        }
                        job?.cancel()
                    }

                    ConnectionState.Connecting -> {
                        disconnectionReason.value = null
                        _deviceData.update {
                            DeviceConnectionState.Connecting
                        }
                    }

                    ConnectionState.Disconnecting -> _deviceData.update {
                        DeviceConnectionState.Disconnected(disconnectionReason.value)
                    }
                }
            }?.launchIn(viewModelScope)
    }

    /**
     * Check for missing services and update the device data.
     *
     * @param api the service API.
     * @param peripheral the connected peripheral.
     */
    private fun checkForMissingServices(api: ServiceApi, peripheral: Peripheral?) =
        api.isMissingServices.onEach { isMissing ->
            (_deviceData.value as? DeviceConnectionState.Connected)?.let { connectedState ->
                _deviceData.update {
                    connectedState.copy(
                        data = connectedState.data.copy(isMissingServices = isMissing)
                    )
                }
            }
            if (!isMissing) {
                connectedDeviceJob?.cancel()
                connectedDeviceJob = api.connectedDevices.onEach { peripheralProfileMap ->
                    peripheral?.let { device ->
                        // Update the profile data
                        peripheralProfileMap[device.address]?.second?.forEach { profileHandler ->
                            updateProfileData(profileHandler)
                        }
                    }
                }.launchIn(viewModelScope)
            }
        }.launchIn(viewModelScope)

    /**
     * Observe and update the data from the profile handler.
     * @param profileHandler the profile handler.
     */
    private fun updateProfileData(profileHandler: ProfileHandler) {
        when (profileHandler.profile) {
            Profile.HTS -> updateHTS(profileHandler)
            Profile.HRS -> updateHRS(profileHandler)
            Profile.BATTERY -> updateBatteryLevel(profileHandler)
            Profile.BPS -> updateBPS(profileHandler)
            else -> { /* TODO: Add more profile modules here */
            }
        }
    }

    /**
     * Update the health thermometer service data.
     *
     * @param profileHandler the profile handler.
     */
    private fun updateHTS(profileHandler: ProfileHandler) {
        profileHandler.getNotification().onEach {
            val htsData = it as HtsData
            updateDeviceData(
                HTSServiceData(
                    data = htsData,
                    temperatureUnit = (_deviceData.value as DeviceConnectionState.Connected).data.serviceData
                        .filterIsInstance<HTSServiceData>()
                        .firstOrNull()?.temperatureUnit ?: TemperatureUnit.CELSIUS
                )
            )
        }.launchIn(viewModelScope)
    }

    /**
     * Update the heart rate service data.
     *
     * @param profileHandler the profile handler.
     */
    private fun updateHRS(profileHandler: ProfileHandler) {
        val zoomInData = (_deviceData.value as DeviceConnectionState.Connected).data.serviceData
            .filterIsInstance<HRSServiceData>()
            .firstOrNull()?.zoomIn ?: false
        profileHandler.getNotification().onEach {
            val hrsData = it as HRSData
            updateDeviceData(
                HRSServiceData(
                    data = listOf(hrsData),
                    zoomIn = zoomInData
                )
            )
        }.launchIn(viewModelScope)
        profileHandler.readCharacteristic()?.onEach {
            val bodySensorLocation = it as Int
            updateDeviceData(
                HRSServiceData(
                    bodySensorLocation = bodySensorLocation,
                    zoomIn = zoomInData
                )
            )
        }?.launchIn(viewModelScope)
    }

    /**
     * Update the battery level.
     *
     * @param profileHandler the profile handler.
     */
    private fun updateBatteryLevel(profileHandler: ProfileHandler) {
        profileHandler.getNotification().onEach {
            val batteryLevel = it as Int
            updateDeviceData(BatteryServiceData(batteryLevel = batteryLevel))
        }.launchIn(viewModelScope)
    }

    /**
     * Update the blood pressure service data.
     *
     * @param profileHandler the profile handler.
     */
    private fun updateBPS(profileHandler: ProfileHandler) {
        updateDeviceData(BPSServiceData(profile = profileHandler.profile))
        profileHandler.getNotification().onEach {
            val bpsData = it as BPSData
            updateDeviceData(
                BPSServiceData(
                    bloodPressureMeasurement = bpsData.bloodPressureMeasurement,
                    intermediateCuffPressure = bpsData.intermediateCuffPressure,
                )
            )
        }.launchIn(viewModelScope)
    }

    /**
     * Update the device data with the given data.
     *
     * @param data the data to update.
     */
    private inline fun <reified T : ProfileServiceData> updateDeviceData(data: T) {
        val updatedData = when (val state = _deviceData.value) {
            is DeviceConnectionState.Connected -> state.data.serviceData.toMutableList().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    removeIf { it is T }
                }
                add(data)
            }

            else -> return
        }
        _deviceData.update {
            (_deviceData.value as DeviceConnectionState.Connected).copy(
                data = (_deviceData.value as DeviceConnectionState.Connected).data.copy(serviceData = updatedData)
            )
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
            is OnRetryClicked -> reconnectDevice(event.device)
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
     *
     * @param deviceAddress the address of the device to reconnect to.
     */
    private fun reconnectDevice(deviceAddress: String) = viewModelScope.launch {
        address = deviceAddress
        _deviceData.update {
            DeviceConnectionState.Idle
        }
        getServiceApi()?.let { connectToPeripheral(deviceAddress) }
    }

    /**
     * Disconnect the device if missing services and navigate back.
     */
    private fun disconnectIfNeededAndNavigate() = viewModelScope.launch {
        if ((_deviceData.value as? DeviceConnectionState.Connected)?.data?.isMissingServices == true) {
            getServiceApi()?.disconnect(address ?: return@launch)
        }
        navigator.navigateUp()
    }

    /**
     * Disconnect the device with the given address and navigate back.
     * @param device the address of the device to disconnect.
     */
    private fun disconnectAndNavigate(device: String) = viewModelScope.launch {
        getServiceApi()?.disconnect(device)
        unbindService()
        navigator.navigateUp()
    }

    /**
     * Update the temperature unit.
     * @param unit the temperature unit.
     */
    private fun updateTemperatureUnit(unit: TemperatureUnit) {
        (_deviceData.value as? DeviceConnectionState.Connected)?.let {
            _deviceData.value = it.copy(
                data = it.data.copy(
                    serviceData = it.data.serviceData.map { service ->
                        if (service is HTSServiceData) service.copy(temperatureUnit = unit) else service
                    }
                )
            )
        }
    }

    /** Switch the zoom event. */
    private fun switchZoomEvent() {
        (_deviceData.value as? DeviceConnectionState.Connected)?.let {
            _deviceData.value = it.copy(
                data = it.data.copy(
                    serviceData = it.data.serviceData.map { service ->
                        if (service is HRSServiceData) service.copy(zoomIn = !service.zoomIn) else service
                    }
                )
            )
        }
    }

    /**
     * Unbind the service.
     */
    fun unbindService() {
        serviceApi?.let { serviceManager.unbindService() }
        serviceApi = null
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }

}
