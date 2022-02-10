package no.nordicsemi.android.cgms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.cgms.data.CGMRepository
import no.nordicsemi.android.cgms.data.CGMServiceCommand
import no.nordicsemi.android.cgms.repository.CGMS_SERVICE_UUID
import no.nordicsemi.android.cgms.view.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class CGMScreenViewModel @Inject constructor(
    private val repository: CGMRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _state = MutableStateFlow<BPSViewState>(NoDeviceState)
    val state = _state.asStateFlow()

    init {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(CGMS_SERVICE_UUID))

        navigationManager.recentResult.onEach {
            if (it.destinationId == ScannerDestinationId) {
                handleArgs(it)
            }
        }.launchIn(viewModelScope)

        repository.data.onEach {
            _state.value = WorkingState(it)
            Log.d("AAATESTAAA", "vm data: $it")
        }.launchIn(viewModelScope)
    }

    private fun handleArgs(args: DestinationResult) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> connectDevice(args.getDevice())
        }.exhaustive
    }

    fun onEvent(event: CGMViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            is OnWorkingModeSelected -> repository.sendNewServiceCommand(event.workingMode)
            NavigateUp -> navigationManager.navigateUp()
        }.exhaustive
    }

    private fun connectDevice(deviceHolder: DiscoveredBluetoothDevice) {
        repository.launch(deviceHolder.device)
    }

    private fun disconnect() {
        repository.sendNewServiceCommand(CGMServiceCommand.DISCONNECT)
    }
}
