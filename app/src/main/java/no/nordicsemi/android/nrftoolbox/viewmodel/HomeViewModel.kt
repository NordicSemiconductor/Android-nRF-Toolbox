package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrftoolbox.repository.ActivitySignals
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.android.toolbox.scanner.changed.DeviceRepository
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import javax.inject.Inject

data class HomeViewState(
    val connectedDevices: Map<Peripheral, List<ProfileHandler>> = emptyMap(),
    val refreshToggle: Boolean = false,
) {
    fun toggleRefresh(): HomeViewState = copy(refreshToggle = !refreshToggle)
}

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val navigator: Navigator,
    activitySignals: ActivitySignals,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        activitySignals.state.onEach {
            _state.update { currentState ->
                currentState.toggleRefresh()
            }
        }.launchIn(viewModelScope)

        // Observe connected devices from the repository
        viewModelScope.launch {
            deviceRepository.connectedDevices.collect { devices ->
                _state.update { currentState ->
                    currentState.copy(connectedDevices = devices) // Update UI state with connected devices
                }
            }
        }
    }

    fun startScanning() {
        navigator.navigateTo(ScannerDestinationId)
    }

}