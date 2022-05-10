package no.nordicsemi.android.bps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.Profile
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.bps.data.BPS_SERVICE_UUID
import no.nordicsemi.android.bps.repository.BPSRepository
import no.nordicsemi.android.bps.view.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.service.SuccessResult
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class BPSViewModel @Inject constructor(
    private val repository: BPSRepository,
    private val navigationManager: NavigationManager,
    private val analytics: AppAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow<BPSViewState>(NoDeviceState)
    val state = _state.asStateFlow()

    init {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(BPS_SERVICE_UUID))

        navigationManager.recentResult.onEach {
            if (it.destinationId == ScannerDestinationId) {
                handleArgs(it)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleArgs(args: DestinationResult) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> connectDevice(args.getDevice())
        }.exhaustive
    }

    fun onEvent(event: BPSViewEvent) {
        when (event) {
            DisconnectEvent -> navigationManager.navigateUp()
            OpenLoggerEvent -> repository.openLogger()
        }.exhaustive
    }

    private fun connectDevice(device: DiscoveredBluetoothDevice) {
        repository.downloadData(device).onEach {
            _state.value = WorkingState(it)

            (it as? SuccessResult)?.let {
                analytics.logEvent(ProfileConnectedEvent(Profile.BPS))
            }
        }.launchIn(viewModelScope)
    }
}
