package no.nordicsemi.android.rscs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.rscs.data.RSCSRepository
import no.nordicsemi.android.rscs.repository.RSCS_SERVICE_UUID
import no.nordicsemi.android.rscs.view.*
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class RSCSViewModel @Inject constructor(
    private val repository: RSCSRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _state = MutableStateFlow<RSCSViewState>(NoDeviceState)
    val state = _state.asStateFlow()

    init {
        if (!repository.isRunning.value) {
            requestBluetoothDevice()
        }

        repository.data.onEach {
            _state.value = WorkingState(it)
        }.launchIn(viewModelScope)
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(RSCS_SERVICE_UUID))

        navigationManager.recentResult.onEach {
            if (it.destinationId == ScannerDestinationId) {
                handleArgs(it)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleArgs(args: DestinationResult) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> repository.launch(args.getDevice().device)
        }.exhaustive
    }

    fun onEvent(event: RSCScreenViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            NavigateUpEvent -> navigationManager.navigateUp()
        }.exhaustive
    }

    private fun disconnect() {
        repository.release()
        navigationManager.navigateUp()
    }
}
