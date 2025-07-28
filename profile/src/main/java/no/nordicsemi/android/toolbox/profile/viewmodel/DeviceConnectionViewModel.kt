package no.nordicsemi.android.toolbox.profile.viewmodel

import android.content.Context
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
import no.nordicsemi.android.lib.profile.common.WorkingMode
import no.nordicsemi.android.lib.profile.csc.SpeedUnit
import no.nordicsemi.android.lib.profile.csc.WheelSize
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.timber.nRFLoggerTree
import no.nordicsemi.android.service.profile.CustomReason
import no.nordicsemi.android.service.profile.DeviceDisconnectionReason
import no.nordicsemi.android.service.profile.ProfileServiceManager
import no.nordicsemi.android.service.profile.ServiceApi
import no.nordicsemi.android.service.profile.StateReason
import no.nordicsemi.android.service.repository.BPSRepository
import no.nordicsemi.android.service.repository.BatteryRepository
import no.nordicsemi.android.service.repository.CGMRepository
import no.nordicsemi.android.service.repository.CSCRepository
import no.nordicsemi.android.service.repository.DFSRepository
import no.nordicsemi.android.service.repository.GLSRepository
import no.nordicsemi.android.service.repository.HRSRepository
import no.nordicsemi.android.service.repository.HTSRepository
import no.nordicsemi.android.service.repository.RSCSRepository
import no.nordicsemi.android.service.repository.ThroughputRepository
import no.nordicsemi.android.service.repository.UartRepository
import no.nordicsemi.android.service.services.ServiceManager
import no.nordicsemi.android.toolbox.profile.DeviceConnectionDestinationId
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.android.toolbox.profile.data.ProfileServiceData
import no.nordicsemi.android.toolbox.profile.data.uart.MacroEol
import no.nordicsemi.android.toolbox.profile.data.uiMapper.TemperatureUnit
import no.nordicsemi.android.toolbox.profile.repository.DeviceRepository
import no.nordicsemi.android.toolbox.profile.repository.UARTPersistentDataSource
import no.nordicsemi.android.toolbox.profile.repository.UartConfigurationDataRepository
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
    data class Disconnected(
        val device: Peripheral? = null,
        val reason: DeviceDisconnectionReason?
    ) : DeviceConnectionState()
}

@HiltViewModel
internal class DeviceConnectionViewModel @Inject constructor(
    private val profileServiceManager: ProfileServiceManager,
    private val navigator: Navigator,
    private val deviceRepository: DeviceRepository,
    @ApplicationContext private val context: Context,
    private val uartConfigurationRepository: UartConfigurationDataRepository,
    private val dataSource: UARTPersistentDataSource,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    val address: String = parameterOf(DeviceConnectionDestinationId)
    private val _deviceData = MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.Idle)
    val deviceData = _deviceData.asStateFlow()
    private var peripheral: Peripheral? = null

    private var logger: nRFLoggerTree? = null
    private var serviceApi: WeakReference<ServiceApi>? = null

    private var job: Job? = null

    /**
     * Bind the service and return the API if successful.
     */
    private suspend fun getServiceApi(): ServiceApi? {
        if (serviceApi == null) {
            serviceApi = WeakReference(profileServiceManager.bindService())
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
            peripheral = it.getPeripheralById(address)
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
        getServiceApi()?.let {
            if (peripheral == null) peripheral = it.getPeripheralById(address)
            if (peripheral?.isConnected != true) {
                profileServiceManager.connectToPeripheral(deviceAddress)
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
                if (peripheral == null) peripheral = api.getPeripheralById(address)
                when (connectionState) {
                    ConnectionState.Connected -> {
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
                            DeviceConnectionState.Disconnected(
                                peripheral,
                                StateReason(connectionState.reason)
                            )
                        }
                    }

                    ConnectionState.Disconnecting, ConnectionState.Closed -> {
                        unbindService()
                        api.disconnectionReason.onEach { reason ->
                            if (reason != null) {
                                _deviceData.update {
                                    DeviceConnectionState.Disconnected(peripheral, reason)
                                }
                            } else {
                                _deviceData.update {
                                    DeviceConnectionState.Disconnected(
                                        peripheral,
                                        CustomReason(DisconnectReason.UNKNOWN)
                                    )
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
    private fun updateProfileData(profileHandler: ServiceManager) {
        when (profileHandler.profile) {
            Profile.BATTERY -> updateBatteryLevel()
            Profile.BPS -> updateBPS()
            Profile.CSC -> updateCSC()
            Profile.CGM -> updateCGM()
            Profile.DFS -> updateDFS()
            Profile.GLS -> updateGLS()
            Profile.HRS -> updateHRS()
            Profile.HTS -> updateHTS()
            Profile.RSCS -> updateRSCS()
            Profile.THROUGHPUT -> updateThroughput()
            Profile.UART -> updateUART()
            Profile.PRX -> {
                TODO()
            }
        }
    }

    private fun updateUART() {
        UartRepository.getData(address).onEach {
            updateDeviceData(it)
        }.launchIn(viewModelScope)

        // Update maximum write value length. Since it will be available as soon as connection is established.
        viewModelScope.launch {
            getServiceApi()?.getMaxWriteValue(address)
                ?.let { UartRepository.updateMaxWriteLength(address, it) }
        }
        dataSource.getConfigurations(address).onEach {
            UartRepository.updateConfiguration(address, it)
        }.launchIn(viewModelScope)

        uartConfigurationRepository.lastConfigurationName.onEach {
            UartRepository.updateSelectedConfigurationName(address, it)
        }.launchIn(viewModelScope)

    }

    private fun updateThroughput() {
        ThroughputRepository.getData(address).onEach {
            updateDeviceData(it)
        }.launchIn(viewModelScope)
        // Update maximum write value length. Since it will be available as soon as connection is established.
        viewModelScope.launch {
            ThroughputRepository.updateMaxWriteValueLength(
                address,
                getServiceApi()?.getMaxWriteValue(address)
            )
        }
    }

    private fun updateDFS() = DFSRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)

    private fun updateCSC() = CSCRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)

    private fun updateRSCS() = RSCSRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)

    private fun updateCGM() = CGMRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)

    /** Update the health thermometer service data. */
    private fun updateHTS() = HTSRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)


    /** Update the heart rate service data. */
    private fun updateHRS() = HRSRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)


    /** Update the battery level. */
    private fun updateBatteryLevel() = BatteryRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)

    /** Update the blood pressure service data. */
    private fun updateBPS() = BPSRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)

    private fun updateGLS() = GLSRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)

    /**
     * Update the device data with the given data.
     *
     * @param data the data to update.
     */
    private inline fun <reified T : ProfileServiceData> updateDeviceData(data: T) {
        val state = _deviceData.value as? DeviceConnectionState.Connected ?: return
        val updatedServiceData = state.data.serviceData.toMutableList().apply {
            val existingIndex = this.indexOfFirst { it is T }
            if (existingIndex != -1) {
                this[existingIndex] = data  // Update the existing entry
            } else this.add(data)// Add a new entry
        }
        _deviceData.update {
            state.copy(data = state.data.copy(serviceData = updatedServiceData))
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
            OpenLoggerEvent -> openLogger()
            is CSCViewEvent.OnSelectedSpeedUnitSelected -> setSpeedUnit(event.selectedSpeedUnit)
            is CSCViewEvent.OnWheelSizeSelected -> setWheelSize(event.wheelSize)

            is GLSViewEvent.OnWorkingModeSelected -> onWorkingModeSelected(
                event.profile,
                event.workingMode
            )

            HRSViewEvent.SwitchZoomEvent -> switchZoomEvent()
            is HTSViewEvent.OnTemperatureUnitSelected -> updateTemperatureUnit(event.value)
            DFSViewEvent.OnAvailableDistanceModeRequest -> viewModelScope.launch {
                DFSRepository.checkAvailableFeatures(address)
            }

            DFSViewEvent.OnCheckDistanceModeRequest -> viewModelScope.launch {
                DFSRepository.checkCurrentDistanceMode(address)
            }

            is DFSViewEvent.OnRangeChangedEvent -> {
                DFSRepository.updateDistanceRange(address, event.range)
            }

            is DFSViewEvent.OnDistanceModeSelected -> {
                viewModelScope.launch {
                    DFSRepository.enableDistanceMode(address, event.mode)
                }
            }

            is DFSViewEvent.OnDetailsSectionParamsSelected -> {
                DFSRepository.updateDetailsSection(address, event.section)
            }

            is DFSViewEvent.OnBluetoothDeviceSelected -> DFSRepository.updateSelectedDevice(
                address,
                event.device
            )

            is RSCSViewEvent.OnSelectedSpeedUnitSelected ->
                RSCSRepository.updateUnitSettings(address, event.rscsUnitSettings)

            is ThroughputEvent.OnWriteData -> viewModelScope.launch {
                ThroughputRepository.sendDataToDK(address, event.writeType)
            }

            // UART events.
            UARTEvent.ClearOutputItems -> UartRepository.clearOutputItems(address)
            UARTEvent.MacroInputSwitchClicked -> TODO()
            is UARTEvent.OnAddConfiguration -> TODO()
            is UARTEvent.OnConfigurationSelected -> TODO()
            is UARTEvent.OnCreateMacro -> TODO()
            UARTEvent.OnDeleteConfiguration -> TODO()
            UARTEvent.OnDeleteMacro -> TODO()
            UARTEvent.OnEditConfiguration -> TODO()
            UARTEvent.OnEditFinished -> TODO()
            is UARTEvent.OnEditMacro -> TODO()
            is UARTEvent.OnRunInput -> {
                sendText(event.text, event.newLineChar)
            }

            is UARTEvent.OnRunMacro -> TODO()
        }
    }

    private fun sendText(text: String, newLineChar: MacroEol) = viewModelScope.launch {
        UartRepository.sendText(address, text, newLineChar)
        //todo: log event in the analytics.
    }

    private fun setSpeedUnit(selectedSpeedUnit: SpeedUnit) {
        CSCRepository.setSpeedUnit(address, selectedSpeedUnit)
    }

    private fun setWheelSize(wheelSize: WheelSize) {
        CSCRepository.setWheelSize(address, wheelSize)
    }

    private fun onWorkingModeSelected(profile: Profile, workingMode: WorkingMode) =
        viewModelScope.launch {
            when (profile) {
                Profile.CGM -> CGMRepository.requestRecord(address, workingMode)
                Profile.GLS -> GLSRepository.requestRecord(address, workingMode)
                else -> {
                    // TODO: Show Error.
                }
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
        getServiceApi()?.disconnect(device)
        unbindService()
        navigator.navigateUp()
    }

    /**
     * Update the temperature unit.
     * @param unit the temperature unit.
     */
    private fun updateTemperatureUnit(unit: TemperatureUnit) {
        HTSRepository.onTemperatureUnitChange(address, unit)
    }

    /** Switch the zoom event. */
    private fun switchZoomEvent() {
        HRSRepository.updateZoomIn(address)
    }

    /**
     * Unbind the service.
     */
    private fun unbindService() {
        serviceApi?.let { profileServiceManager.unbindService() }
        serviceApi = null
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }

}
