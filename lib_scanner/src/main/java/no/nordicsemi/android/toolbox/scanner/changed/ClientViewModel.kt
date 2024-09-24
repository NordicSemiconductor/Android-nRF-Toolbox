package no.nordicsemi.android.toolbox.scanner.changed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.HtsData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.TemperatureUnit
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.android.toolbox.scanner.ConnectDeviceDestinationId
import no.nordicsemi.android.toolbox.scanner.view.hts.view.DisconnectEvent
import no.nordicsemi.android.toolbox.scanner.view.hts.view.NavigateUp
import no.nordicsemi.android.toolbox.scanner.view.hts.view.OnRetryClicked
import no.nordicsemi.android.toolbox.scanner.view.hts.view.OnTemperatureUnitSelected
import no.nordicsemi.android.toolbox.scanner.view.hts.view.OpenLoggerEvent
import no.nordicsemi.android.toolbox.scanner.view.hts.view.ProfileScreenViewEvent
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import java.lang.ref.WeakReference
import javax.inject.Inject

data class ClientData(
    val peripheral: Peripheral? = null,
    val connectionState: ConnectionState? = null,
    val htsServiceData: HTSServiceData = HTSServiceData(),
    val batteryLevel: Int? = null,
    val isMissingServices: Boolean = false,
)

@HiltViewModel
internal class ClientViewModel @Inject constructor(
    private val serviceManager: ServiceManager,
    private val navigator: Navigator,
    private val deviceRepository: DeviceRepository,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val _clientData = MutableStateFlow(ClientData())
    val clientData = _clientData.asStateFlow()

    val address: String = parameterOf(ConnectDeviceDestinationId)
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

    /**
     * Unbind the service.
     */
    private fun unbindService() {
        serviceApi?.let { serviceManager.unbindService() }
        serviceApi = null
    }

    /**
     * Connect to the peripheral with the given address. Before connecting, the service must be bound.
     * The service will be started if not already running.
     * @param deviceAddress the address of the peripheral to connect to.
     */
    fun connectToPeripheral(deviceAddress: String) = viewModelScope.launch {
        address = deviceAddress
        getServiceApi()?.let { api ->
            val peripheral = api.getPeripheralById(deviceAddress)
            api.connectedDevices.onEach { device ->
                if (!device.containsKey(peripheral)) {
                    // If not connected, attempt connection
                    serviceManager.connectToPeripheral(deviceAddress)
                }
                updateServiceData(deviceAddress)

            }.launchIn(viewModelScope)
        }
    }

    /**
     * Update the service data, including connection state and peripheral data.
     * @param deviceAddress the address of the connected device.
     */
    private suspend fun updateServiceData(deviceAddress: String) {
        // Observe the handlers for the connected device
        getServiceApi()?.let { api ->
            api.getPeripheralConnectionState(deviceAddress)?.onEach { connectionState ->
                _clientData.value = _clientData.value.copy(
                    connectionState = connectionState,
                )
                if (connectionState == ConnectionState.Connected) {
                    api.isMissingServices.onEach { isMissing ->
                        if (isMissing) {
                            // Update missing service flag.
                            _clientData.value = _clientData.value.copy(
                                isMissingServices = true,
                            )

                        } else {
                            // Observe the data from the connected device
                            updateConnectedData(api, deviceAddress)
                        }
                    }.launchIn(viewModelScope)
                }
            }?.launchIn(viewModelScope)
        }
    }

    /**
     * Update the connected data, including the peripheral, profile data, and battery level.
     * @param api the service API.
     * @param deviceAddress the address of the connected device.
     */
    private fun updateConnectedData(
        api: ServiceApi,
        deviceAddress: String
    ) {
        api.connectedDevices.onEach { peripheralProfileMap ->
            deviceRepository.updateConnectedDevices(peripheralProfileMap)
            val peripheral = api.getPeripheralById(deviceAddress)
            _clientData.value = _clientData.value.copy(
                peripheral = peripheral,
            )
            peripheral?.let { device ->
                // Update the profile data
                peripheralProfileMap[device]?.forEach { profileHandler ->
                    updateProfileData(profileHandler)
                }
                // Update battery level
                api.batteryLevel.collect {
                    _clientData.value = _clientData.value.copy(
                        batteryLevel = it,
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Observe and update the data from the profile handler.
     * @param profileHandler the profile handler.
     */
    private fun updateProfileData(profileHandler: ProfileHandler) {
        when (profileHandler.profileModule) {
            ProfileModule.HTS -> {
                profileHandler.observeData().onEach {
                    _clientData.value = _clientData.value.copy(
                        htsServiceData = _clientData.value.htsServiceData.copy(
                            data = it as HtsData,
                        )
                    )
                }.launchIn(viewModelScope)
            }
            // TODO: Add more profile modules here
            else -> TODO()
        }
    }

    /**
     * Handle click events from the view.
     */
    fun onClickEvent(event: ProfileScreenViewEvent) {
        // Handle click events
        when (event) {
            is DisconnectEvent -> disconnectAndNavigate(event.device)
            NavigateUp -> disconnectIfNeededAndNavigate()
            is OnRetryClicked -> connectToPeripheral(event.device)
            is OnTemperatureUnitSelected -> updateTemperatureUnit(event.value)
            OpenLoggerEvent -> TODO()
        }

    }

    private fun disconnectIfNeededAndNavigate() = viewModelScope.launch {
        if (_clientData.value.isMissingServices) {
            serviceApi?.get()?.apply {
                disconnectPeripheral(address!!)
            }
        }
        navigator.navigateUp()
    }

    private fun disconnectAndNavigate(device: String) = viewModelScope.launch {
        getServiceApi()?.disconnectPeripheral(device)
        // Unbind the service.
        unbindService()
        // navigate back
        navigator.navigateUp()
    }

    private fun updateTemperatureUnit(unit: TemperatureUnit) {
        // Handle temperature unit selection
        _clientData.value = _clientData.value.copy(
            htsServiceData = _clientData.value.htsServiceData.copy(
                temperatureUnit = unit
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }

}
