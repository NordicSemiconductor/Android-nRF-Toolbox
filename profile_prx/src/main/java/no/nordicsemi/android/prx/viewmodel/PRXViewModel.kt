package no.nordicsemi.android.prx.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.prx.data.PRXDataHolder
import no.nordicsemi.android.prx.view.DisconnectEvent
import no.nordicsemi.android.prx.view.PRXScreenViewEvent
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class PRXViewModel @Inject constructor(
    private val dataHolder: PRXDataHolder
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: PRXScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
        }.exhaustive
    }

    private fun onDisconnectButtonClick() {
        finish()
        dataHolder.clear()
    }
}
