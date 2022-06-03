package no.nordicsemi.android.prx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.Profile
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.prx.data.PRX_SERVICE_UUID
import no.nordicsemi.android.prx.repository.PRXRepository
import no.nordicsemi.android.prx.view.*
import no.nordicsemi.android.service.ConnectedResult
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class PRXViewModel @Inject constructor(
    private val repository: PRXRepository,
    private val navigationManager: NavigationManager,
    private val analytics: AppAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow<PRXViewState>(NoDeviceState)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.isRunning.firstOrNull() == false) {
                requestBluetoothDevice()
            }
        }

        repository.data.onEach {
            _state.value = WorkingState(it)

            (it as? ConnectedResult)?.let {
                analytics.logEvent(ProfileConnectedEvent(Profile.PRX))
            }
        }.launchIn(viewModelScope)
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(PRX_SERVICE_UUID))

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

    fun onEvent(event: PRXScreenViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            TurnOffAlert -> repository.disableAlarm()
            TurnOnAlert -> repository.enableAlarm()
            NavigateUpEvent -> navigationManager.navigateUp()
            OpenLoggerEvent -> repository.openLogger()
        }.exhaustive
    }

    private fun disconnect() {
        repository.release()
        navigationManager.navigateUp()
    }
}
