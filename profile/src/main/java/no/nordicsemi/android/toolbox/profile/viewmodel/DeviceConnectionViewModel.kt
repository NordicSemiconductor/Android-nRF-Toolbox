package no.nordicsemi.android.toolbox.profile.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
import no.nordicsemi.android.service.repository.ChannelSoundingRepository
import no.nordicsemi.android.service.repository.DFSRepository
import no.nordicsemi.android.service.repository.GLSRepository
import no.nordicsemi.android.service.repository.HRSRepository
import no.nordicsemi.android.service.repository.HTSRepository
import no.nordicsemi.android.service.repository.LBSRepository
import no.nordicsemi.android.service.repository.RSCSRepository
import no.nordicsemi.android.service.repository.ThroughputRepository
import no.nordicsemi.android.service.repository.UartRepository
import no.nordicsemi.android.service.services.ServiceManager
import no.nordicsemi.android.toolbox.profile.DeviceConnectionDestinationId
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.android.toolbox.profile.data.ProfileServiceData
import no.nordicsemi.android.toolbox.profile.data.uart.MacroEol
import no.nordicsemi.android.toolbox.profile.data.uart.UARTConfiguration
import no.nordicsemi.android.toolbox.profile.data.uart.UARTMacro
import no.nordicsemi.android.toolbox.profile.data.uiMapper.TemperatureUnit
import no.nordicsemi.android.toolbox.profile.repository.DeviceRepository
import no.nordicsemi.android.toolbox.profile.repository.uartXml.UartConfigurationRepository
import no.nordicsemi.android.toolbox.profile.repository.channelSounding.ChannelSoundingManager
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
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
    private val uartConfigurationRepository: UartConfigurationRepository,
    @ApplicationContext private val context: Context,
    private val channelSoundingManager: ChannelSoundingManager,
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
            Profile.CHANNEL_SOUNDING -> updateChannelSounding()
            Profile.LBS -> updateLBS()
            Profile.PRX -> {
                TODO()
            }

        }
    }

    private fun updateLBS() = LBSRepository.getData(address).onEach {
        updateDeviceData(it)
    }.launchIn(viewModelScope)

    private fun updateUART() {
        UartRepository.getData(address).onEach {
            updateDeviceData(it)
        }.launchIn(viewModelScope)

        // Get the last configuration name from the data store.
        uartConfigurationRepository
            .getLastConfigurationName().onEach { name ->
                name?.let { UartRepository.updateSelectedConfigurationName(address, it) }
            }.launchIn(viewModelScope)

        // Get all configurations for the device.
        uartConfigurationRepository.getAllConfigurations().onEach { uartConfigurations ->
            UartRepository.loadPreviousConfigurations(address, uartConfigurations)
        }.launchIn(viewModelScope)

        // Update maximum write value length. Since it will be available as soon as connection is established.
        viewModelScope.launch(Dispatchers.IO) {
            getServiceApi()?.getMaxWriteValue(address)
                ?.let { UartRepository.updateMaxWriteLength(address, it) }
        }
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

    private fun updateChannelSounding() {
        ChannelSoundingRepository.getData(address).onEach {
            updateDeviceData(it)
        }.launchIn(viewModelScope)
        if (Build.VERSION.SDK_INT >= 36) {
            viewModelScope.launch {
                getServiceApi()?.createBonding(address)
                try {
                    channelSoundingManager.addDeviceToRangingSession(address)
                } catch (e: Exception) {
                    Timber.e(" ${e.message}")
                }
            }
        } else {
            Timber.tag("AAA").d("Channel Sounding is not available")
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
    fun onClickEvent(event: ProfileUiEvent) {
        // Handle click events
        when (event) {
            is DisconnectEvent -> disconnectAndNavigate(event.device)
            NavigateUp -> disconnectIfNeededAndNavigate()
            is OnRetryClicked -> reconnectDevice(event.device)
            OpenLoggerEvent -> openLogger()
            is CSCEvent.OnSelectedSpeedUnitSelected -> setSpeedUnit(event.selectedSpeedUnit)
            is CSCEvent.OnWheelSizeSelected -> setWheelSize(event.wheelSize)

            is GLSEvent.OnWorkingModeSelected -> onWorkingModeSelected(
                event.profile,
                event.workingMode
            )

            HRSEvent.SwitchZoomEvent -> switchZoomEvent()
            is HTSEvent.OnTemperatureUnitSelected -> updateTemperatureUnit(event.value)
            DFSEvent.OnAvailableDistanceModeRequest -> viewModelScope.launch {
                DFSRepository.checkAvailableFeatures(address)
            }

            DFSEvent.OnCheckDistanceModeRequest -> viewModelScope.launch {
                DFSRepository.checkCurrentDistanceMode(address)
            }

            is DFSEvent.OnRangeChangedEvent -> {
                DFSRepository.updateDistanceRange(address, event.range)
            }

            is DFSEvent.OnDistanceModeSelected -> {
                viewModelScope.launch {
                    DFSRepository.enableDistanceMode(address, event.mode)
                }
            }

            is DFSEvent.OnDetailsSectionParamsSelected -> {
                DFSRepository.updateDetailsSection(address, event.section)
            }

            is DFSEvent.OnBluetoothDeviceSelected -> DFSRepository.updateSelectedDevice(
                address,
                event.device
            )

            is RSCSEvent.OnSelectedSpeedUnitSelected ->
                RSCSRepository.updateUnitSettings(address, event.rscsUnitSettings)

            is ThroughputEvent.OnWriteData -> viewModelScope.launch {
                ThroughputRepository.sendDataToDK(address, event.writeType)
            }

            // UART events.
            UARTEvent.ClearOutputItems -> UartRepository.clearOutputItems(address) // working.
            is UARTEvent.OnAddConfiguration -> onAddConfiguration(event.name) // TODO: check if it is working.

            is UARTEvent.OnConfigurationSelected -> onConfigurationSelected(event.configuration)
            is UARTEvent.OnCreateMacro -> addNewMacro(event.macroName)
            is UARTEvent.OnDeleteConfiguration -> deleteConfiguration(event.configuration)
            UARTEvent.OnDeleteMacro -> onDeleteMacro()
            is UARTEvent.OnEditConfiguration -> onEditConfiguration()
            UARTEvent.OnEditFinished -> onEditFinished()
            is UARTEvent.OnEditMacro -> onEditMacro(event.position)
            is UARTEvent.OnRunInput -> {
                sendText(event.text, event.newLineChar)
            }

            is UARTEvent.OnRunMacro -> runMacro(event.macro)
            is LBSEvent.OnButtonStateChanged -> {
                LBSRepository.updateButtonState(address, event.buttonState)
            }

            is LBSEvent.OnLedStateChanged -> {
                viewModelScope.launch {
                    LBSRepository.writeToBlinkyLED(address, event.ledState)
                }
            }
        }
    }

    private fun onDeleteMacro() = viewModelScope.launch(Dispatchers.IO) {
        UartRepository.onDeleteMacro(address)
    }

    private fun onEditFinished() {
        viewModelScope.launch {
            UartRepository.onEditFinished(address)
        }
    }

    private fun addNewMacro(macroName: UARTMacro) = viewModelScope.launch(Dispatchers.IO) {
        val newConfig = UartRepository.addOrEditMacro(address, macroName)
        if (newConfig != null) {
            uartConfigurationRepository.insertConfiguration(newConfig)
        }
    }

    private fun onEditMacro(position: Int) = viewModelScope.launch {
        // Update the configuration in the UART repository.
        UartRepository.onEditMacro(address, position)
    }

    private fun onEditConfiguration() = viewModelScope.launch {
        // Update the configuration in the UART repository.
        UartRepository.onEditConfiguration(address)
    }

    private fun runMacro(macro: UARTMacro) = viewModelScope.launch {
        UartRepository.runMacro(address, macro)
    }

    private fun onAddConfiguration(name: String) = viewModelScope.launch(Dispatchers.IO) {
        // Update the configuration in the UART repository.
        UartRepository.updateSelectedConfigurationName(address, name)
        // Add configuration to the database.
        val configurationId =
            uartConfigurationRepository.insertConfiguration(UARTConfiguration(null, name))
                ?: return@launch
        // Add configuration to the repository.
        UartRepository.addConfiguration(address, UARTConfiguration(configurationId.toInt(), name))

        // Save the configuration name in the data store.
        uartConfigurationRepository.saveLastConfigurationNameToDataSource(name)
    }

    private fun onConfigurationSelected(configuration: UARTConfiguration) = viewModelScope.launch {
        UartRepository.updateSelectedConfigurationName(address, configuration.name)
        // Update the selected configuration in the datastore.
        uartConfigurationRepository.saveLastConfigurationNameToDataSource(configuration.name)
    }

    private fun deleteConfiguration(configuration: UARTConfiguration) =
        viewModelScope.launch(Dispatchers.IO) {
            // delete the configuration from the list.
            UartRepository.deleteConfiguration(address, configuration)
            // remove the selected configuration if it is deleted.
            UartRepository.removeSelectedConfiguration(address)
            // delete the configuration from the database.
            uartConfigurationRepository.deleteConfiguration(configuration)
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
