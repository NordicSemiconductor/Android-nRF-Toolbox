package no.nordicsemi.android.toolbox.scanner.changed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.HtsData
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import java.lang.ref.WeakReference
import javax.inject.Inject

data class ClientData(
    val peripheral: Peripheral? = null,
    val connectionState: ConnectionState? = null,
    val htsServiceData: HTSServiceData? = null,
    val batteryLevel: Int? = null,
)

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val serviceManager: ServiceManager,
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
                        getProfile()
                    }
                }?.launchIn(viewModelScope)

            }
        }
    }

    private fun getProfile() {
        profileService?.get()?.connectedDevices?.onEach { peripheralListMap ->
            val peripheral =
                deviceAddress?.let { it1 -> profileService?.get()?.getPeripheralById(it1) }
            peripheralListMap[peripheral]?.forEach { profileHandler ->
                _clientData.value = _clientData.value.copy(peripheral = peripheral)
                when (profileHandler.profileModule) {
                    ProfileModule.HTS -> {
                        profileHandler.observeData().onEach {
                            _clientData.value = _clientData.value.copy(
                                htsServiceData = HTSServiceData(
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

}
