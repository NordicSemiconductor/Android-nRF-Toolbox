package no.nordicsemi.android.uart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.uart.data.DisconnectCommand
import no.nordicsemi.android.uart.data.SendTextCommand
import no.nordicsemi.android.uart.data.UARTRepository
import no.nordicsemi.android.uart.view.DisplayDataState
import no.nordicsemi.android.uart.view.LoadingState
import no.nordicsemi.android.uart.view.OnCreateMacro
import no.nordicsemi.android.uart.view.OnDeleteMacro
import no.nordicsemi.android.uart.view.OnDisconnectButtonClick
import no.nordicsemi.android.uart.view.OnRunMacro
import no.nordicsemi.android.uart.view.UARTState
import no.nordicsemi.android.uart.view.UARTViewEvent
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class UARTViewModel @Inject constructor(
    private val repository: UARTRepository
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> UARTState(LoadingState)
            BleManagerStatus.OK -> UARTState(DisplayDataState(data))
            BleManagerStatus.DISCONNECTED -> UARTState(DisplayDataState(data), false)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, UARTState(LoadingState))

    fun onEvent(event: UARTViewEvent) {
        when (event) {
            is OnCreateMacro -> repository.addNewMacro(event.macro)
            is OnDeleteMacro -> repository.deleteMacro(event.macro)
            OnDisconnectButtonClick -> repository.sendNewCommand(DisconnectCommand)
            is OnRunMacro -> repository.sendNewCommand(SendTextCommand(event.macro.command))
        }.exhaustive
    }
}
