package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.service.services.ServiceManager
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.android.toolbox.profile.repository.DeviceRepository
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import javax.inject.Inject

internal data class HomeViewState(
    val connectedDevices: Map<String, Pair<Peripheral, List<ServiceManager>>> = emptyMap(),
)

private const val GITHUB_REPO_URL = "https://github.com/NordicSemiconductor/Android-nRF-Toolbox.git"
private const val NORDIC_DEV_ZONE_URL = "https://devzone.nordicsemi.com/"

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

    fun onClickEvent(event: UiEvent) {
        when (event) {
            UiEvent.OnConnectDeviceClick -> navigator.navigateTo(ScannerDestinationId)
            is UiEvent.OnDeviceClick -> navigator.navigateTo(
                ProfileDestinationId, event.deviceAddress
            )

            UiEvent.OnGitHubClick -> navigator.open(GITHUB_REPO_URL.toUri())
            UiEvent.OnNordicDevZoneClick -> navigator.open(NORDIC_DEV_ZONE_URL.toUri())
        }
    }

}