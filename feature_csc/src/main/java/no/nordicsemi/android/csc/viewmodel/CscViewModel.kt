package no.nordicsemi.android.csc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import no.nordicsemi.android.csc.events.CSCServiceEvent
import no.nordicsemi.android.csc.events.CrankDataChanged
import no.nordicsemi.android.csc.events.OnBatteryLevelChanged
import no.nordicsemi.android.csc.events.OnDistanceChangedEvent
import no.nordicsemi.android.csc.service.CSCDataReadBroadcast
import no.nordicsemi.android.csc.view.CSCViewConnectedState
import no.nordicsemi.android.csc.view.CSCViewEvent
import no.nordicsemi.android.csc.view.CSCViewNotConnectedState
import no.nordicsemi.android.csc.view.CSCViewState
import no.nordicsemi.android.csc.view.OnBluetoothDeviceSelected
import no.nordicsemi.android.csc.view.OnConnectButtonClick
import no.nordicsemi.android.csc.view.OnDisconnectButtonClick
import no.nordicsemi.android.csc.view.OnMovedToScannerScreen
import no.nordicsemi.android.csc.view.OnSelectedSpeedUnitSelected
import no.nordicsemi.android.csc.view.OnShowEditWheelSizeDialogButtonClick
import no.nordicsemi.android.csc.view.OnWheelSizeSelected
import no.nordicsemi.android.scanner.tools.SelectedBluetoothDeviceHolder
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class CscViewModel @Inject constructor(
    private val localBroadcast: CSCDataReadBroadcast,
    private val deviceHolder: SelectedBluetoothDeviceHolder
) : ViewModel() {

    val state = MutableStateFlow(createInitialState())

    init {
        localBroadcast.events.onEach {
            withContext(Dispatchers.Main) { consumeEvent(it) }
        }.launchIn(viewModelScope)
    }

    private fun createInitialState(): CSCViewState {
        return deviceHolder.device?.let { CSCViewConnectedState() } ?: CSCViewNotConnectedState()
    }

    private fun consumeEvent(event: CSCServiceEvent) {
        val newValue = when (event) {
            is CrankDataChanged -> createNewState(event)
            is OnBatteryLevelChanged -> createNewState(event)
            is OnDistanceChangedEvent -> createNewState(event)
        }
        state.value = newValue
    }

    private fun createNewState(event: CrankDataChanged): CSCViewConnectedState {
        return state.value.ensureConnectedState().copy(
            cadence = event.crankCadence,
            gearRatio = event.gearRatio
        )
    }

    private fun createNewState(event: OnBatteryLevelChanged): CSCViewConnectedState {
        return state.value.ensureConnectedState().copy(
            batteryLevel = event.batteryLevel
        )
    }

    private fun createNewState(event: OnDistanceChangedEvent): CSCViewConnectedState {
        return state.value.ensureConnectedState().copy(
            speed = event.speed,
            distance = event.distance,
            totalDistance = event.totalDistance
        )
    }

    fun onEvent(event: CSCViewEvent) {
        when (event) {
            is OnSelectedSpeedUnitSelected -> onSelectedSpeedUnit(event)
            OnShowEditWheelSizeDialogButtonClick -> onShowDialogEvent()
            is OnWheelSizeSelected -> onWheelSizeChanged(event)
            OnDisconnectButtonClick -> onDisconnectButtonClick()
            OnConnectButtonClick -> onConnectButtonClick()
            OnMovedToScannerScreen -> onOnMovedToScannerScreen()
            is OnBluetoothDeviceSelected -> onBluetoothDeviceSelected()
        }.exhaustive
    }

    private fun onSelectedSpeedUnit(event: OnSelectedSpeedUnitSelected) {
        state.tryEmit(state.value.ensureConnectedState().copy(selectedSpeedUnit = event.selectedSpeedUnit))
    }

    private fun onShowDialogEvent() {
        state.tryEmit(state.value.ensureConnectedState().copy(showDialog = true))
    }

    private fun onWheelSizeChanged(event: OnWheelSizeSelected) {
        localBroadcast.setWheelSize(event.wheelSize)
        state.tryEmit(state.value.ensureConnectedState().copy(
            showDialog = false,
            wheelSize = event.wheelSizeDisplayInfo
        ))
    }

    private fun onDisconnectButtonClick() {
        state.tryEmit(CSCViewNotConnectedState())
    }

    private fun onConnectButtonClick() {
        state.tryEmit(state.value.ensureDisconnectedState().copy(showScannerDialog = true))
    }

    private fun onOnMovedToScannerScreen() {
        state.tryEmit(state.value.ensureDisconnectedState().copy(showScannerDialog = false))
    }

    private fun onBluetoothDeviceSelected() {
        state.tryEmit(CSCViewConnectedState())
    }
}
