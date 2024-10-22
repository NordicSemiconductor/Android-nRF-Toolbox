package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.toolbox.libs.profile.DeviceConnectionDestinationId
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.repository.DeviceRepository
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import javax.inject.Inject

internal data class HomeViewState(
    val connectedDevices: Map<String, Pair<Peripheral, List<ProfileHandler>>> = emptyMap(),
)

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val navigator: Navigator,
    deviceRepository: DeviceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        // Observe connected devices from the repository
        deviceRepository.connectedDevices.onEach { devices ->
            _state.update { currentState ->
                currentState.copy(connectedDevices = devices)
            }
        }.launchIn(viewModelScope)
    }

    fun onClickEvent(event: HomeViewEvent) {
        when (event) {
            HomeViewEvent.OnConnectDeviceClick -> navigator.navigateTo(ScannerDestinationId)
            is HomeViewEvent.OnDeviceClick -> navigator.navigateTo(
                DeviceConnectionDestinationId, event.deviceAddress
            )
        }
    }

}