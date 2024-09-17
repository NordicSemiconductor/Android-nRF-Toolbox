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

    private var profileService: WeakReference<ProfileService.LocalBinder>? = null

    // Bind the service when necessary
    fun bindService() {
        viewModelScope.launch {
            synchronousBind()
        }
    }

    private suspend fun synchronousBind() {
        profileService = WeakReference(serviceManager.bindService())
    }

    fun unbindService() {
        profileService?.let { serviceManager.unbindService() }
        profileService = null
    }

    // Function to initiate a connection with a peripheral via the service
    fun connectToPeripheral(deviceAddress: String) {
        viewModelScope.launch {
            this@ClientViewModel.deviceAddress = deviceAddress
            if (profileService == null) {
                synchronousBind()
            }
            profileService?.get()?.apply {
                connectPeripheral(deviceAddress, viewModelScope)
                peripheralConnectionState(deviceAddress)?.onEach {
                    _clientData.value = _clientData.value.copy(connectionState = it)
                    if (it == ConnectionState.Connected) {
                        updateServiceData()

                        // Update repository with the new connected device and its handlers
                        connectedDevices.onEach {
                            deviceRepository.updateConnectedDevices(it)
                        }.launchIn(viewModelScope)
                    }
                }?.launchIn(viewModelScope)
            }
        }
    }

    private fun updateServiceData() {
        profileService?.get()?.connectedDevices?.onEach { peripheralListMap ->
            val peripheral =
                deviceAddress?.let { it1 -> profileService?.get()?.getPeripheralById(it1) }
            peripheralListMap[peripheral]?.forEach { profileHandler ->
                _clientData.value = _clientData.value.copy(peripheral = peripheral)
                when (profileHandler.profileModule) {
                    ProfileModule.HTS -> {
                        profileHandler.observeData().onEach {
                            _clientData.value = _clientData.value.copy(
                                htsServiceData = _clientData.value.htsServiceData.copy(
                                    data = it as HtsData,
                                    deviceName = peripheral?.name ?: peripheral?.address,
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
        }?.launchIn(viewModelScope)
    }

    fun onClickEvent(event: ProfileScreenViewEvent) {
        // Handle click events
        when (event) {
            is DisconnectEvent -> {
                // Disconnect from the peripheral
                viewModelScope.launch {
                    profileService?.get()?.disconnectPeripheral(event.device)
                }
            }

            NavigateUp -> {
                navigator.navigateUp()
            }

            OnRetryClicked -> TODO()
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

}
