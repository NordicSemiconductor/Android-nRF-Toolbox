package no.nordicsemi.android.toolbox.scanner.changed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.HtsData
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
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
)

@HiltViewModel
internal class ClientViewModel @Inject constructor(
    private val serviceManager: ServiceManager,
    private val navigator: Navigator,
    private val deviceRepository: DeviceRepository, // Inject the repository
) : ViewModel() {
    private val _clientData = MutableStateFlow(ClientData())
    val clientData = _clientData.asStateFlow()

    private var deviceAddress: String? = null

    private var serviceApi: WeakReference<ServiceApi>? = null

    init {
        viewModelScope.launch {
            bindService()
        }
    }

    private suspend fun bindService() {
        serviceApi = WeakReference(serviceManager.bindService())
    }

    private fun unbindService() {
        serviceApi?.let { serviceManager.unbindService() }
        serviceApi = null
    }

    // Function to initiate a connection with a peripheral via the service
    fun connectToPeripheral(deviceAddress: String) {
        this.deviceAddress = deviceAddress
        serviceManager.connectToPeripheral(deviceAddress)
        updateServiceData(deviceAddress)
    }

    private fun updateServiceData(deviceAddress: String) {
        // Observe the handlers for the connected device
        serviceApi?.get()?.apply {
            getPeripheralConnectionState(deviceAddress)?.onEach { connectionState ->
                _clientData.value = _clientData.value.copy(
                    connectionState = connectionState,
                )
                if (connectionState == ConnectionState.Connected) {
                    connectedDevices.onEach {
                        deviceRepository.updateConnectedDevices(it)
                        val peripheral = this.getPeripheralById(deviceAddress)
                        _clientData.value = _clientData.value.copy(
                            peripheral = peripheral,
                        )
                        it[peripheral]?.forEach { profileHandler ->
                            updateProfileData(profileHandler)
                        }
                    }.launchIn(viewModelScope)
                }
            }?.launchIn(viewModelScope)
        }
    }

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

            ProfileModule.BATTERY -> {
                // Handle battery service
                profileHandler.observeData().onEach {
                    _clientData.value = _clientData.value.copy(
                        batteryLevel = it as Int,
                    )
                }.launchIn(viewModelScope)
            }

            ProfileModule.CSC -> TODO()
            ProfileModule.HRS -> TODO()
            ProfileModule.RSCS -> TODO()
            ProfileModule.PRX -> TODO()
            ProfileModule.CGM -> TODO()
            ProfileModule.UART -> TODO()
        }
    }

    fun onClickEvent(event: ProfileScreenViewEvent) {
        // Handle click events
        when (event) {
            is DisconnectEvent -> {
                // Disconnect from the peripheral
                viewModelScope.launch {
                    serviceApi?.get()?.apply {
                        // Disconnect the peripheral
                        disconnectPeripheral(event.device)
                        // Unbind the service.
                        unbindService()
                    }
                    // navigate back
                    navigator.navigateUp()
                }
            }

            NavigateUp -> {
                navigator.navigateUp()
            }

            OnRetryClicked -> {
                // Retry connection
                connectToPeripheral(deviceAddress!!)
            }

            is OnTemperatureUnitSelected -> {
                // Handle temperature unit selection
                _clientData.value = _clientData.value.copy(
                    htsServiceData = _clientData.value.htsServiceData.copy(
                        temperatureUnit = event.value
                    )
                )
            }

            OpenLoggerEvent -> TODO()
        }

    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }

}
