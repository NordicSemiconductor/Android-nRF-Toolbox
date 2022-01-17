package no.nordicsemi.android.cgms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.cgms.data.CGMRepository
import no.nordicsemi.android.cgms.data.CGMServiceCommand
import no.nordicsemi.android.cgms.view.CGMState
import no.nordicsemi.android.cgms.view.CGMViewEvent
import no.nordicsemi.android.cgms.view.DisconnectEvent
import no.nordicsemi.android.cgms.view.DisplayDataState
import no.nordicsemi.android.cgms.view.LoadingState
import no.nordicsemi.android.cgms.view.OnWorkingModeSelected
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class CGMScreenViewModel @Inject constructor(
    private val repository: CGMRepository
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> CGMState(LoadingState)
            BleManagerStatus.OK -> CGMState(DisplayDataState(data))
            BleManagerStatus.DISCONNECTED -> CGMState(DisplayDataState(data), false)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, CGMState(LoadingState))

    fun onEvent(event: CGMViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            is OnWorkingModeSelected -> repository.sendNewServiceCommand(event.workingMode)
        }.exhaustive
    }

    private fun disconnect() {
        repository.clear()
        repository.sendNewServiceCommand(CGMServiceCommand.DISCONNECT)
    }
}
