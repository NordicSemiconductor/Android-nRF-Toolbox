package no.nordicsemi.android.cgms.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.cgms.data.CGMDataHolder
import no.nordicsemi.android.cgms.view.CGMViewEvent
import no.nordicsemi.android.cgms.view.DisconnectEvent
import no.nordicsemi.android.cgms.view.OnWorkingModeSelected
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class CGMScreenViewModel @Inject constructor(
    private val dataHolder: CGMDataHolder
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: CGMViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            is OnWorkingModeSelected -> dataHolder.requestNewWorkingMode(event.workingMode)
        }.exhaustive
    }

    private fun disconnect() {
        finish()
        dataHolder.clear()
    }
}
