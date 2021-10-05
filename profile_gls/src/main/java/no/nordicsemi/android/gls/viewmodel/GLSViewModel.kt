package no.nordicsemi.android.gls.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.gls.data.GLSDataHolder
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
    private val dataHolder: GLSDataHolder
) : CloseableViewModel() {

    val state = dataHolder.data
    private var lastSelectedMode = state.value.selectedMode

    init {
        dataHolder.data.onEach {
            if (lastSelectedMode == it.selectedMode) {
                return@onEach
            }
            lastSelectedMode = it.selectedMode
            when (it.selectedMode) {
                WorkingMode.ALL -> glsManager.requestAllRecords()
                WorkingMode.LAST -> glsManager.requestLastRecord()
                WorkingMode.FIRST -> glsManager.requestFirstRecord()
            }.exhaustive
        }.launchIn(GlobalScope)
    }

    fun onEvent(event: GLSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            is OnWorkingModeSelected -> dataHolder.setNewWorkingMode(event.workingMode)
        }.exhaustive
    }

    fun bondDevice() {
        if (deviceHolder.isBondingRequired()) {
            deviceHolder.bondDevice()
        } else {
            connectDevice()
        }
    }

    private fun connectDevice() {
        deviceHolder.device?.let {
            glsManager.connect(it)
                .useAutoConnect(false)
                .retry(3, 100)
                .enqueue()
        }
    }

    private fun disconnect() {
        finish()
        deviceHolder.forgetDevice()
        dataHolder.clear()
    }
}
