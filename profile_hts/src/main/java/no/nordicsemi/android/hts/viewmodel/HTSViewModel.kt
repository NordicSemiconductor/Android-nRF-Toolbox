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
        htsRepository.setOnScreen(true)
        startHtsService()
    }

    fun startHtsService() {
        connectionProvider.profile.onEach {
            if (it == null) {
                return@onEach
            }

            peripheral = it.peripheralDetails.peripheral
            htsRepository.apply {
                peripheral = it.peripheralDetails.peripheral
                remoteService = it.peripheralDetails.serviceData
                getConnection(viewModelScope)
                if (!this.data.value.isServiceRunning) launchHtsService()
            }
        }.launchIn(viewModelScope)
        // Check the Bluetooth connection status and reestablish the device connection if Bluetooth is reconnected.
        connectionProvider.bleState.drop(1).onEach { state ->
            // If the Bluetooth adapter has been disabled, disconnect the device.
            if (state == Manager.State.POWERED_OFF) {
                peripheral?.disconnect()
            } else if (state == Manager.State.POWERED_ON) {
                // Reconnect to the peripheral.
                peripheral?.let {
                    connectionProvider.connectAndObservePeripheral(
                        it,
                        scope = viewModelScope
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: HTSScreenViewEvent) {
        when (event) {
            is NavigateUp -> navigator.navigateUp()
            is DisconnectEvent -> {
                viewModelScope.launch {
                    try {
                        if (peripheral?.isConnected == true) {
                            connectionProvider.disconnect(peripheral!!, viewModelScope)
                            htsRepository.disconnect()
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                    // Navigate back
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
                        connectionProvider.connectAndObservePeripheral(
                            it,
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
        htsRepository.setOnScreen(false)
    }

}