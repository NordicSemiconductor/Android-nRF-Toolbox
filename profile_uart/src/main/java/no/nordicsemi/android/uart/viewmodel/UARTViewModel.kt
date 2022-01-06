package no.nordicsemi.android.uart.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.uart.data.UARTRepository
import no.nordicsemi.android.uart.data.UARTServiceCommand
import no.nordicsemi.android.uart.view.*
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class UARTViewModel @Inject constructor(
    private val dataHolder: UARTRepository
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: UARTViewEvent) {
        when (event) {
            is OnCreateMacro -> dataHolder.addNewMacro(event.macro)
            is OnDeleteMacro -> dataHolder.deleteMacro(event.macro)
            OnDisconnectButtonClick -> finish()
            is OnRunMacro -> dataHolder.sendNewCommand(UARTServiceCommand(event.macro.command))
        }.exhaustive
    }
}
