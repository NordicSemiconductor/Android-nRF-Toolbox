package no.nordicsemi.android.csc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.csc.data.CSCRepository
import no.nordicsemi.android.csc.data.DisconnectCommand
import no.nordicsemi.android.csc.data.SetWheelSizeCommand
import no.nordicsemi.android.csc.repository.CSCService
import no.nordicsemi.android.csc.view.CSCViewEvent
import no.nordicsemi.android.csc.view.DisplayDataState
import no.nordicsemi.android.csc.view.LoadingState
import no.nordicsemi.android.csc.view.OnDisconnectButtonClick
import no.nordicsemi.android.csc.view.OnSelectedSpeedUnitSelected
import no.nordicsemi.android.csc.view.OnWheelSizeSelected
import no.nordicsemi.android.navigation.CancelDestinationResult
import no.nordicsemi.android.navigation.ForwardDestination
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.navigation.ParcelableArgument
import no.nordicsemi.android.navigation.SuccessDestinationResult
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class CSCViewModel @Inject constructor(
    private val repository: CSCRepository,
    private val serviceManager: ServiceManager,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val args
        get() = navigationManager.getResult(ScannerDestinationId)
    private val device
        get() = ((args as SuccessDestinationResult).argument as ParcelableArgument).value as DiscoveredBluetoothDevice

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> LoadingState
            BleManagerStatus.OK,
            BleManagerStatus.DISCONNECTED -> DisplayDataState(data)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, LoadingState)

    init {
        when (args) {
            CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> serviceManager.startService(CSCService::class.java, device)
            null -> navigationManager.navigateTo(ForwardDestination(ScannerDestinationId))
        }.exhaustive

        repository.status.onEach {
            if (it == BleManagerStatus.DISCONNECTED) {
                navigationManager.navigateUp()
            }
        }.launchIn(viewModelScope)
    }

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
