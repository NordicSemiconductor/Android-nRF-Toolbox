package no.nordicsemi.android.nrftoolbox.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.toolbox.libs.profile.DeviceConnectionDestinationId
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.repository.DeviceRepository
import no.nordicsemi.android.toolbox.libs.profile.service.ServiceManager
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewModel
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import javax.inject.Inject

data class HomeViewState(
    val connectedDevices: Map<Peripheral, List<ProfileHandler>> = emptyMap(),
)

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    serviceManager: ServiceManager,
    private val navigator: Navigator,
    deviceRepository: DeviceRepository,
    @ApplicationContext context: Context,
) : DeviceConnectionViewModel(serviceManager, navigator, deviceRepository, context) {
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

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }

}