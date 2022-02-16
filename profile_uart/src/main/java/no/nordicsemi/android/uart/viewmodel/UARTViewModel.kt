package no.nordicsemi.android.uart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.uart.data.UARTMacro
import no.nordicsemi.android.uart.data.UART_SERVICE_UUID
import no.nordicsemi.android.uart.repository.UARTRepository
import no.nordicsemi.android.uart.view.*
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class UARTViewModel @Inject constructor(
    private val repository: UARTRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _state = MutableStateFlow(UARTViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.isRunning.firstOrNull() == false) {
                requestBluetoothDevice()
            }
        }

        repository.data.onEach {
            _state.value = _state.value.copy(uartManagerState = WorkingState(it))
        }.launchIn(viewModelScope)
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(UART_SERVICE_UUID))

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

    fun onEvent(event: UARTViewEvent) {
        when (event) {
            is OnCreateMacro -> addNewMacro(event.macro)
            is OnDeleteMacro -> deleteMacro(event.macro)
            DisconnectEvent -> disconnect()
            is OnRunMacro -> repository.runMacro(event.macro)
            NavigateUp -> navigationManager.navigateUp()
        }.exhaustive
    }

    private fun addNewMacro(macro: UARTMacro) {
        _state.tryEmit(_state.value.copy(macros = _state.value.macros + macro))
    }

    private fun deleteMacro(macro: UARTMacro) {
        val macros = _state.value.macros.toMutableList().apply {
            remove(macro)
        }
        _state.tryEmit(_state.value.copy(macros = macros))
    }

    private fun disconnect() {
        repository.release()
        navigationManager.navigateUp()
    }
}
