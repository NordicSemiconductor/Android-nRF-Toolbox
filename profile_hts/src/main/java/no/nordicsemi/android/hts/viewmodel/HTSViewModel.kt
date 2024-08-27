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
import no.nordicsemi.android.hts.repository.HTSRepository
import no.nordicsemi.android.toolbox.libs.profile.ConnectionProvider
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.Manager
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Health Thermometer Service.
 */
@HiltViewModel
internal class HTSViewModel @Inject constructor(
    private val connectionProvider: ConnectionProvider,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val htsRepository: HTSRepository,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    val state = htsRepository.data
    private var peripheral: Peripheral? = null

    init {
        startHtsService()
    }

    private fun startHtsService() {
        connectionProvider.profile.onEach {
            if (it == null) {
                return@onEach
            }
            peripheral = it.remoteService.peripheral
            htsRepository.peripheral = peripheral
            htsRepository.remoteService = it.remoteService.serviceData
            htsRepository.getConnection(viewModelScope)
            htsRepository.launch()
        }.launchIn(viewModelScope)
        // Check the Bluetooth connection status and reestablish the device connection if Bluetooth is reconnected.
        connectionProvider.bleState.drop(1).onEach { state ->
            // If the Bluetooth adapter has been disabled, disconnect the device.
            if (state == Manager.State.POWERED_OFF) {
                peripheral?.disconnect()
            } else if (state == Manager.State.POWERED_ON) {
                // Reconnect to the peripheral.
                peripheral?.address?.let {
                    connectionProvider.connectToDevice(
                        it,
                        scope = viewModelScope
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: HTSScreenViewEvent) {
        when (event) {
            is NavigateUp, is DisconnectEvent -> {
                viewModelScope.launch {
                    // Navigate back
                    try {
                        if (peripheral?.isConnected == true) {
                            htsRepository.disconnect()
                            peripheral?.let { connectionProvider.disconnect(it, viewModelScope) }
                            viewModelScope.cancel()
                            connectionProvider.clear()
                        }
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
                    peripheral?.let {
                        connectionProvider.connectToDevice(
                            it.address,
                            scope = viewModelScope
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

}