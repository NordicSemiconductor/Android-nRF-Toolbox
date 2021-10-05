package no.nordicsemi.android.hrs.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.hrs.data.HRSDataHolder
import no.nordicsemi.android.hrs.view.DisconnectEvent
import no.nordicsemi.android.hrs.view.HRSScreenViewEvent
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import javax.inject.Inject

@HiltViewModel
internal class HRSViewModel @Inject constructor(
    private val dataHolder: HRSDataHolder
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: HRSScreenViewEvent) {
        (event as? DisconnectEvent)?.let {
            onDisconnectButtonClick()
        }
    }

    private fun onDisconnectButtonClick() {
        finish()
        dataHolder.clear()
    }
}
