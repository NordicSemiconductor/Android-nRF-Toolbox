package no.nordicsemi.android.bps.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.bps.data.BPSDataHolder
import no.nordicsemi.android.bps.view.BPSScreenViewEvent
import no.nordicsemi.android.bps.view.DisconnectEvent
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class BPSViewModel @Inject constructor(
    private val dataHolder: BPSDataHolder
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: BPSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
        }.exhaustive
    }

    private fun onDisconnectButtonClick() {
        finish()
        dataHolder.clear()
    }
}
