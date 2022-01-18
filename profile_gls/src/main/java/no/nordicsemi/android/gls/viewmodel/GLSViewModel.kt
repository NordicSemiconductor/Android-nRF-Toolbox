package no.nordicsemi.android.gls.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.gls.data.GLSRepository
import no.nordicsemi.android.gls.data.WorkingMode
import no.nordicsemi.android.gls.repository.GLSManager
import no.nordicsemi.android.gls.view.DisplayDataState
import no.nordicsemi.android.gls.view.GLSState
import no.nordicsemi.android.gls.view.LoadingState
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ConnectionObserverAdapter
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class GLSViewModel @Inject constructor(
    private val glsManager: GLSManager,
    private val deviceHolder: SelectedBluetoothDeviceHolder,
    private val repository: GLSRepository
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> GLSState(LoadingState)
            BleManagerStatus.OK -> GLSState(DisplayDataState(data))
            BleManagerStatus.DISCONNECTED -> GLSState(DisplayDataState(data), false)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, GLSState(LoadingState))

    init {
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
    }

    fun onEvent(event: GLSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            is OnWorkingModeSelected -> requestData(event.workingMode)
        }.exhaustive
    }

    fun connectDevice() {
        deviceHolder.device?.let {
            glsManager.connect(it.device)
                .useAutoConnect(false)
                .retry(3, 100)
                .enqueue()
        }
    }

    private fun requestData(mode: WorkingMode) {
        when (mode) {
            WorkingMode.ALL -> glsManager.requestAllRecords()
            WorkingMode.LAST -> glsManager.requestLastRecord()
            WorkingMode.FIRST -> glsManager.requestFirstRecord()
        }.exhaustive
    }

    private fun disconnect() {
        deviceHolder.forgetDevice()
        glsManager.disconnect().enqueue()
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
    }
}
