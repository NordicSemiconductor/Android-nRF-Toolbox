package no.nordicsemi.android.csc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.csc.data.CSCRepository
import no.nordicsemi.android.csc.data.DisconnectCommand
import no.nordicsemi.android.csc.data.SetWheelSizeCommand
import no.nordicsemi.android.csc.view.CSCState
import no.nordicsemi.android.csc.view.CSCViewEvent
import no.nordicsemi.android.csc.view.DisplayDataState
import no.nordicsemi.android.csc.view.LoadingState
import no.nordicsemi.android.csc.view.OnDisconnectButtonClick
import no.nordicsemi.android.csc.view.OnSelectedSpeedUnitSelected
import no.nordicsemi.android.csc.view.OnWheelSizeSelected
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class CSCViewModel @Inject constructor(
    private val repository: CSCRepository
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> CSCState(LoadingState)
            BleManagerStatus.OK -> CSCState(DisplayDataState(data))
            BleManagerStatus.DISCONNECTED -> CSCState(DisplayDataState(data), false)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, CSCState(LoadingState))

    fun onEvent(event: CSCViewEvent) {
        when (event) {
            is OnSelectedSpeedUnitSelected -> onSelectedSpeedUnit(event)
            is OnWheelSizeSelected -> onWheelSizeChanged(event)
            OnDisconnectButtonClick -> onDisconnectButtonClick()
        }.exhaustive
    }

    private fun onSelectedSpeedUnit(event: OnSelectedSpeedUnitSelected) {
        repository.setSpeedUnit(event.selectedSpeedUnit)
    }

    private fun onWheelSizeChanged(event: OnWheelSizeSelected) {
        repository.sendNewServiceCommand(SetWheelSizeCommand(event.wheelSize))
    }

    private fun onDisconnectButtonClick() {
        repository.sendNewServiceCommand(DisconnectCommand)
        repository.clear()
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
    }
}
