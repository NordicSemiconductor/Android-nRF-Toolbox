package no.nordicsemi.android.toolbox.libs.profile.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.SavedStateHandle
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
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.timber.nRFLoggerTree
import no.nordicsemi.android.toolbox.libs.profile.DeviceConnectionDestinationId
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.bps.BPSData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.HtsData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.TemperatureUnit
import no.nordicsemi.android.toolbox.libs.profile.data.service.BPSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.BatteryServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.HRSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.ProfileServiceData
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.repository.DeviceRepository
import no.nordicsemi.android.toolbox.libs.profile.repository.HRSRepository
import no.nordicsemi.android.toolbox.libs.profile.service.CustomReason
import no.nordicsemi.android.toolbox.libs.profile.service.DeviceDisconnectionReason
import no.nordicsemi.android.toolbox.libs.profile.service.ServiceApi
import no.nordicsemi.android.toolbox.libs.profile.service.ServiceManager
import no.nordicsemi.android.toolbox.libs.profile.service.StateReason
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import java.lang.ref.WeakReference
import javax.inject.Inject

internal data class DeviceData(
    val peripheral: Peripheral? = null,
    val connectionState: ConnectionState? = null,
    val serviceData: List<ProfileServiceData> = emptyList(),
    val isMissingServices: Boolean = false,
    val disconnectionReason: ConnectionState.Disconnected.Reason? = null,
)

internal sealed class DeviceConnectionState {
    data object Idle : DeviceConnectionState()
    data object Connecting : DeviceConnectionState()
    data class Connected(val data: DeviceData) : DeviceConnectionState()
    data class Disconnected(val reason: DeviceDisconnectionReason?) : DeviceConnectionState()
}

@HiltViewModel
internal class DeviceConnectionViewModel @Inject constructor(
    private val serviceManager: ServiceManager,
    private val navigator: Navigator,
    private val deviceRepository: DeviceRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    val address: String = parameterOf(DeviceConnectionDestinationId)
    private val _deviceData = MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.Idle)
    val deviceData = _deviceData.asStateFlow()

    private var logger: nRFLoggerTree? = null
    private var serviceApi: WeakReference<ServiceApi>? = null

    private var job: Job? = null

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
        connectToPeripheral(address)
    }

    /**
     * Observe connected devices and update the repository.
     */
    private fun observeConnectedDevices() = viewModelScope.launch {
        getServiceApi()?.let {
            val peripheral = it.getPeripheralById(address)
            it.connectedDevices
                .onEach { peripheralProfileMap ->
                    deviceRepository.updateConnectedDevices(peripheralProfileMap)
                    peripheralProfileMap[peripheral?.address]?.second?.forEach { profileHandler ->
                        updateProfileData(profileHandler)
                    }
                }.launchIn(viewModelScope)
                .apply {
                    updateConnectionState(it, address, peripheral?.isConnected == true)
                }
        }
    }

    /**
     * Connect to the peripheral with the given address. Before connecting, the service must be bound.
     * The service will be started if not already running.
     * @param deviceAddress the address of the peripheral to connect to.
     */
    private fun connectToPeripheral(deviceAddress: String) = viewModelScope.launch {
        // Connect to the peripheral
        getServiceApi()?.let { api ->
            val peripheral = api.getPeripheralById(deviceAddress)
            if (peripheral?.isConnected != true) {
                serviceManager.connectToPeripheral(deviceAddress)
            }
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
                        }.apply { checkForMissingServices(api) }
                    }

                    is ConnectionState.Disconnected -> {
                        _deviceData.update {
                            DeviceConnectionState.Disconnected(StateReason(connectionState.reason))
                        }
                    }

                    ConnectionState.Disconnecting, ConnectionState.Closed -> {
                        unbindService()
                        api.disconnectionReason.onEach { reason ->
                            if (reason != null) {
                                _deviceData.update {
                                    DeviceConnectionState.Disconnected(reason)
                                }
                            } else {
                                _deviceData.update {
                                    DeviceConnectionState.Disconnected(CustomReason(DisconnectReason.UNKNOWN))
                                }
                            }
                        }.launchIn(viewModelScope)
                        job?.cancel()
                    }

                    ConnectionState.Connecting -> {
                        _deviceData.update {
                            DeviceConnectionState.Connecting
                        }
                    }
                }
            }?.launchIn(viewModelScope)
    }

    /**
     * Check for missing services.
     */
    private fun checkForMissingServices(api: ServiceApi) =
        api.isMissingServices.onEach { isMissing ->
            (_deviceData.value as? DeviceConnectionState.Connected)?.let { connectedState ->
                _deviceData.update {
                    connectedState.copy(
                        data = connectedState.data.copy(isMissingServices = isMissing)
                    )
                }
            }
        }.launchIn(viewModelScope)

    /**
     * Observe and update the data from the profile handler.
     * @param profileHandler the profile handler.
     */
    private fun updateProfileData(profileHandler: ProfileHandler) {
        when (profileHandler.profile) {
            Profile.HTS -> updateHTS(profileHandler)
            Profile.HRS -> updateHRS()
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
     */
    private fun updateHRS() = HRSRepository.data.onEach {
        updateDeviceData(
            HRSServiceData(
                profile = Profile.HRS,
                data = it.data,
                bodySensorLocation = it.bodySensorLocation,
                zoomIn = it.zoomIn
            )
        )
    }.launchIn(viewModelScope)


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
        _deviceData.update {
            DeviceConnectionState.Idle
        }
        getServiceApi()?.let {
            connectToPeripheral(deviceAddress)
            updateConnectionState(it, deviceAddress, false)
        }
    }

    /**
     * Disconnect the device if missing services and navigate back.
     */
    private fun disconnectIfNeededAndNavigate() = viewModelScope.launch {
        if ((_deviceData.value as? DeviceConnectionState.Connected)?.data?.isMissingServices == true) {
            getServiceApi()?.disconnect(address)
        }
        navigator.navigateUp()
    }

    /**
     * Disconnect the device with the given address and navigate back.
     * @param device the address of the device to disconnect.
     */
    private fun disconnectAndNavigate(device: String) = viewModelScope.launch {
        // clear the data.
        HRSRepository.clear()
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
        HRSRepository.updateZoomIn()
    }

    /**
     * Unbind the service.
     */
    private fun unbindService() {
        serviceApi?.let { serviceManager.unbindService() }
        serviceApi = null
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }

}
