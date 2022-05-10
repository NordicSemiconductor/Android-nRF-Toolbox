package no.nordicsemi.android.csc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.csc.data.CSC_SERVICE_UUID
import no.nordicsemi.android.csc.repository.CSCRepository
import no.nordicsemi.android.csc.view.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class CSCViewModel @Inject constructor(
    private val repository: CSCRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _state = MutableStateFlow(CSCViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.isRunning.firstOrNull() == false) {
                requestBluetoothDevice()
            }
        }

        repository.data.onEach {
            _state.value = _state.value.copy(cscManagerState = WorkingState(it))
        }.launchIn(viewModelScope)
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(CSC_SERVICE_UUID))

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

    fun onEvent(event: CSCViewEvent) {
        when (event) {
            is OnSelectedSpeedUnitSelected -> setSpeedUnit(event.selectedSpeedUnit)
            is OnWheelSizeSelected -> repository.setWheelSize(event.wheelSize)
            OnDisconnectButtonClick -> disconnect()
            NavigateUp -> navigationManager.navigateUp()
            OpenLogger -> repository.openLogger()
        }.exhaustive
    }

    private fun setSpeedUnit(speedUnit: SpeedUnit) {
        _state.value = _state.value.copy(speedUnit = speedUnit)
    }

    private fun disconnect() {
        repository.release()
        navigationManager.navigateUp()
    }
}
