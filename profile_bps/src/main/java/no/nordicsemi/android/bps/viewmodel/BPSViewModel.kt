package no.nordicsemi.android.bps.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.bps.data.BPSRepository
import no.nordicsemi.android.bps.repository.BPSManager
import no.nordicsemi.android.bps.view.BPSScreenViewEvent
import no.nordicsemi.android.bps.view.DisconnectEvent
import no.nordicsemi.android.bps.view.DisplayDataState
import no.nordicsemi.android.bps.view.LoadingState
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.navigation.ParcelableArgument
import no.nordicsemi.android.navigation.SuccessDestinationResult
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ConnectionObserverAdapter
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class BPSViewModel @Inject constructor(
    private val bpsManager: BPSManager,
    private val repository: BPSRepository,
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
        bpsManager.setConnectionObserver(object : ConnectionObserverAdapter() {
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

    fun onEvent(event: BPSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
        }.exhaustive
    }

    fun connectDevice() {
        bpsManager.connect(device.device)
            .useAutoConnect(false)
            .retry(3, 100)
            .enqueue()
    }

    private fun onDisconnectButtonClick() {
        bpsManager.disconnect().enqueue()
        repository.clear()
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
    }
}
