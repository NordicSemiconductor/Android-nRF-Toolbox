package no.nordicsemi.android.csc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.csc.service.CSCDataReadBroadcast
import no.nordicsemi.android.csc.view.CSCViewEvent
import no.nordicsemi.android.csc.view.OnDisconnectButtonClick
import no.nordicsemi.android.csc.view.OnSelectedSpeedUnitSelected
import no.nordicsemi.android.csc.view.OnShowEditWheelSizeDialogButtonClick
import no.nordicsemi.android.csc.view.OnWheelSizeSelected
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class CscViewModel @Inject constructor(
    private val localBroadcast: CSCDataReadBroadcast
) : ViewModel() {

    val state = MutableStateFlow(CSCData())

    init {
        localBroadcast.events.onEach {
            withContext(Dispatchers.Main) { state.value = it }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: CSCViewEvent) {
        when (event) {
            is OnSelectedSpeedUnitSelected -> onSelectedSpeedUnit(event)
            OnShowEditWheelSizeDialogButtonClick -> onShowDialogEvent()
            is OnWheelSizeSelected -> onWheelSizeChanged(event)
            OnDisconnectButtonClick -> onDisconnectButtonClick()
        }.exhaustive
    }

    private fun onSelectedSpeedUnit(event: OnSelectedSpeedUnitSelected) {
        state.tryEmit(state.value.copy(selectedSpeedUnit = event.selectedSpeedUnit))
    }

    private fun onShowDialogEvent() {
        state.tryEmit(state.value.copy(showDialog = true))
    }

    private fun onWheelSizeChanged(event: OnWheelSizeSelected) {
        localBroadcast.setWheelSize(event.wheelSize)
        state.tryEmit(state.value.copy(
            showDialog = false,
            wheelSize = event.wheelSizeDisplayInfo
        ))
    }

    private fun onDisconnectButtonClick() {
        state.tryEmit(state.value.copy(isScreenActive = false))
    }
}
