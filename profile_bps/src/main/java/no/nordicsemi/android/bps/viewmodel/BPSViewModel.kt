package no.nordicsemi.android.bps.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.bps.data.BPSRepository
import no.nordicsemi.android.bps.repository.BPSManager
import no.nordicsemi.android.bps.view.BPSScreenViewEvent
import no.nordicsemi.android.bps.view.BPSState
import no.nordicsemi.android.bps.view.DisconnectEvent
import no.nordicsemi.android.bps.view.DisplayDataState
import no.nordicsemi.android.bps.view.LoadingState
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ConnectionObserverAdapter
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class BPSViewModel @Inject constructor(
    private val bpsManager: BPSManager,
    private val deviceHolder: SelectedBluetoothDeviceHolder,
    private val repository: BPSRepository
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> BPSState(LoadingState)
            BleManagerStatus.OK -> BPSState(DisplayDataState(data))
            BleManagerStatus.DISCONNECTED -> BPSState(DisplayDataState(data), false)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, BPSState(LoadingState))

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
    }

    fun onEvent(event: BPSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
        }.exhaustive
    }

    fun connectDevice() {
        bpsManager.connect(deviceHolder.device!!.device)
            .useAutoConnect(false)
            .retry(3, 100)
            .enqueue()
    }

    private fun onDisconnectButtonClick() {
        bpsManager.disconnect().enqueue()
        deviceHolder.forgetDevice()
        repository.clear()
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
    }
}
