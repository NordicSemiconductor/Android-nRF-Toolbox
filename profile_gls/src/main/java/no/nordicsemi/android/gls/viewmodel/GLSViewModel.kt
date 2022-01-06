package no.nordicsemi.android.gls.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.gls.data.GLSRepository
import no.nordicsemi.android.gls.data.WorkingMode
import no.nordicsemi.android.gls.repository.GLSManager
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class GLSViewModel @Inject constructor(
    private val glsManager: GLSManager,
    private val deviceHolder: SelectedBluetoothDeviceHolder,
    private val dataHolder: GLSRepository
) : CloseableViewModel() {

    val state = dataHolder.data

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
        finish()
        deviceHolder.forgetDevice()
        dataHolder.clear()
    }
}
