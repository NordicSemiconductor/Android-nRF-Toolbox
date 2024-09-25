package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrftoolbox.repository.ActivitySignals
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.ConnectDeviceDestinationId
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.ClientViewModel
import no.nordicsemi.android.toolbox.libs.profile.repository.DeviceRepository
import no.nordicsemi.android.toolbox.libs.profile.repository.ServiceManager
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
    serviceManager: ServiceManager,
    private val navigator: Navigator,
    activitySignals: ActivitySignals,
    deviceRepository: DeviceRepository,
) : ClientViewModel(serviceManager, navigator, deviceRepository) {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        activitySignals.state.onEach {
            _state.update { currentState ->
                currentState.toggleRefresh()
            }
        }.launchIn(viewModelScope)

        // Observe connected devices from the repository
        deviceRepository.connectedDevices.onEach { devices ->
            _state.update { currentState ->
                currentState.copy(connectedDevices = devices)
            }
        }.launchIn(viewModelScope)
    }

    fun onClickEvent(event: HomeViewEvent) {
        when (event) {
            HomeViewEvent.AddDeviceClick -> navigator.navigateTo(ScannerDestinationId)
            is HomeViewEvent.OnConnectedDeviceClick -> navigator.navigateTo(
                ConnectDeviceDestinationId, event.deviceAddress
            )
        }
    }

}