package no.nordicsemi.android.bps.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.bps.data.BPSRepository
import no.nordicsemi.android.bps.repository.BPSManager
import no.nordicsemi.android.bps.view.BPSScreenViewEvent
import no.nordicsemi.android.bps.view.DisconnectEvent
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class BPSViewModel @Inject constructor(
    private val bpsManager: BPSManager,
    private val deviceHolder: SelectedBluetoothDeviceHolder,
    private val dataHolder: BPSRepository
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: BPSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
        }.exhaustive
    }

    fun connectDevice() {
        deviceHolder.device?.let {
            bpsManager.connect(it.device)
                .useAutoConnect(false)
                .retry(3, 100)
                .enqueue()
        }
    }

    private fun onDisconnectButtonClick() {
        finish()
        deviceHolder.forgetDevice()
        dataHolder.clear()
    }
}
