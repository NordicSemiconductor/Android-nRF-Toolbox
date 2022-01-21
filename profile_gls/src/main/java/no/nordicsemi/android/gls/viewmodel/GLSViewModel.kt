package no.nordicsemi.android.gls.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.gls.data.GLSRepository
import no.nordicsemi.android.gls.data.WorkingMode
import no.nordicsemi.android.gls.repository.GLSManager
import no.nordicsemi.android.gls.repository.GLS_SERVICE_UUID
import no.nordicsemi.android.gls.view.DisplayDataState
import no.nordicsemi.android.gls.view.LoadingState
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ConnectionObserverAdapter
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class GLSViewModel @Inject constructor(
    private val glsManager: GLSManager,
    private val repository: GLSRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> LoadingState
            BleManagerStatus.OK,
            BleManagerStatus.DISCONNECTED -> DisplayDataState(data)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, LoadingState)

    init {
        navigationManager.navigateTo(ForwardDestination(ScannerDestinationId), UUIDArgument(ScannerDestinationId, GLS_SERVICE_UUID))

        navigationManager.recentResult.onEach {
            if (it.destinationId == ScannerDestinationId) {
                handleArgs(it)
            }
        }.launchIn(viewModelScope)

        glsManager.setConnectionObserver(object : ConnectionObserverAdapter() {
            override fun onDeviceConnected(device: BluetoothDevice) {
                super.onDeviceConnected(device)
                repository.setNewStatus(BleManagerStatus.OK)
            }

            override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
                super.onDeviceFailedToConnect(device, reason)
                repository.setNewStatus(BleManagerStatus.DISCONNECTED)
            }

            override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
                super.onDeviceDisconnected(device, reason)
                repository.setNewStatus(BleManagerStatus.DISCONNECTED)
            }
        })

        repository.status.onEach {
            if (it == BleManagerStatus.DISCONNECTED) {
                navigationManager.navigateUp()
            }
        }.launchIn(viewModelScope)
    }

    private fun handleArgs(args: DestinationResult) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> connectDevice(args.getDevice())
        }.exhaustive
    }

    fun onEvent(event: GLSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            is OnWorkingModeSelected -> requestData(event.workingMode)
        }.exhaustive
    }

    private fun connectDevice(device: DiscoveredBluetoothDevice) {
        glsManager.connect(device.device)
            .useAutoConnect(false)
            .retry(3, 100)
            .enqueue()
    }

    private fun requestData(mode: WorkingMode) {
        when (mode) {
            WorkingMode.ALL -> glsManager.requestAllRecords()
            WorkingMode.LAST -> glsManager.requestLastRecord()
            WorkingMode.FIRST -> glsManager.requestFirstRecord()
        }.exhaustive
    }

    private fun disconnect() {
        glsManager.disconnect().enqueue()
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
    }
}
