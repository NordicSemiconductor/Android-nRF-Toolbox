package no.nordicsemi.android.rscs.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.rscs.data.RSCSRepository
import no.nordicsemi.android.rscs.view.DisconnectEvent
import no.nordicsemi.android.rscs.view.RSCScreenViewEvent
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class RSCSViewModel @Inject constructor(
    private val dataHolder: RSCSRepository
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: RSCScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
        }.exhaustive
    }

    private fun onDisconnectButtonClick() {
        finish()
        dataHolder.clear()
    }
}
