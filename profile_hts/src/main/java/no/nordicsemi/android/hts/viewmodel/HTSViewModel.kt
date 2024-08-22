package no.nordicsemi.android.hts.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.hts.repository.HTSRepository
import no.nordicsemi.android.toolbox.libs.profile.DeviceConnectionManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.Manager
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Health Thermometer Service.
 */
@HiltViewModel
internal class HTSViewModel @Inject constructor(
    private val connectionManager: DeviceConnectionManager,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val htsRepository: HTSRepository,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val htsParam = parameterOf(HTSDestinationId)
    val state = htsRepository.data
    private val peripheral: Peripheral? = htsParam.remoteService.peripheral

    init {
        htsRepository.setOnScreen(true)
        htsRepository.peripheral = peripheral
        htsRepository.remoteService = htsParam.remoteService.serviceData
        htsRepository.getConnection(viewModelScope)
        htsRepository.launch(htsParam.remoteService)

        // Check the Bluetooth connection status and reestablish the device connection if Bluetooth is reconnected.
        connectionManager.state.drop(1).onEach { state ->
            // If the Bluetooth adapter has been disabled, disconnect the device.
            if (state == Manager.State.POWERED_OFF) {
                peripheral?.disconnect()
            } else if (state == Manager.State.POWERED_ON) {
                // Reconnect to the peripheral.
                connectionManager.connectToDevice(peripheral!!, scope = viewModelScope)
            }
        }.launchIn(viewModelScope)

    }

    fun onEvent(event: HTSScreenViewEvent) {
        when (event) {
            is NavigateUp, is DisconnectEvent -> {
                viewModelScope.launch {
                    // Navigate back
                    try {
                        if (peripheral?.isConnected == true) htsRepository.disconnect()
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                    navigator.navigateUp()
                }
            }

            OpenLoggerEvent -> {
                // Open the loggers screen.
            }

            is OnTemperatureUnitSelected -> {
                htsRepository.onTemperatureUnitChanged(event.value)
            }

            OnRetryClicked -> {
                // Retry the connection.
                viewModelScope.launch {
                    connectionManager.connectToDevice(peripheral!!, scope = viewModelScope)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        htsRepository.setOnScreen(false)
    }

}