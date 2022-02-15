package no.nordicsemi.android.hts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.hts.data.HTSRepository
import no.nordicsemi.android.hts.repository.HTS_SERVICE_UUID
import no.nordicsemi.android.hts.view.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class HTSViewModel @Inject constructor(
    private val repository: HTSRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _state = MutableStateFlow(HTSViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.isRunning.firstOrNull() == false) {
                requestBluetoothDevice()
            }
        }

        repository.data.onEach {
            _state.value = _state.value.copy(htsManagerState = WorkingState(it))
        }.launchIn(viewModelScope)
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(HTS_SERVICE_UUID))

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

    fun onEvent(event: HTSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            is OnTemperatureUnitSelected -> onTemperatureUnitSelected(event)
            NavigateUp -> navigationManager.navigateUp()
        }.exhaustive
    }

    private fun disconnect() {
        repository.release()
        navigationManager.navigateUp()
    }

    private fun onTemperatureUnitSelected(event: OnTemperatureUnitSelected) {
        _state.value = _state.value.copy(temperatureUnit = event.value)
    }
}
