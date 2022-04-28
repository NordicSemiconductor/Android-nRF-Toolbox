package no.nordicsemi.android.hrs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.hrs.data.HRS_SERVICE_UUID
import no.nordicsemi.android.hrs.service.HRSRepository
import no.nordicsemi.android.hrs.view.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.service.SuccessResult
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class HRSViewModel @Inject constructor(
    private val repository: HRSRepository,
    private val navigationManager: NavigationManager,
    private val analytics: AppAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow<HRSViewState>(NoDeviceState)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.isRunning.firstOrNull() == false) {
                requestBluetoothDevice()
            }
        }

        repository.data.onEach {
            val zoomIn = (_state.value as? WorkingState)?.zoomIn ?: false
            _state.value = WorkingState(it, zoomIn)

            (it as? SuccessResult)?.let {
                analytics.logEvent(ProfileConnectedEvent.HRS)
            }
        }.launchIn(viewModelScope)
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(HRS_SERVICE_UUID))

        navigationManager.recentResult.onEach {
            if (it.destinationId == ScannerDestinationId) {
                handleArgs(it)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleArgs(args: DestinationResult) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> repository.launch(args.getDevice())
        }.exhaustive
    }

    fun onEvent(event: HRSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            NavigateUpEvent -> navigationManager.navigateUp()
            OpenLoggerEvent -> repository.openLogger()
            SwitchZoomEvent -> onZoomButtonClicked()
        }.exhaustive
    }

    private fun onZoomButtonClicked() {
        (_state.value as? WorkingState)?.let {
            _state.value = it.copy(zoomIn = !it.zoomIn)
        }
    }

    private fun disconnect() {
        repository.release()
        navigationManager.navigateUp()
    }
}
