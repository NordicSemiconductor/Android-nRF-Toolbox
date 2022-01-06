package no.nordicsemi.android.prx.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.prx.data.DisableAlarm
import no.nordicsemi.android.prx.data.EnableAlarm
import no.nordicsemi.android.prx.data.PRXRepository
import no.nordicsemi.android.prx.view.DisconnectEvent
import no.nordicsemi.android.prx.view.PRXScreenViewEvent
import no.nordicsemi.android.prx.view.TurnOffAlert
import no.nordicsemi.android.prx.view.TurnOnAlert
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class PRXViewModel @Inject constructor(
    private val dataHolder: PRXRepository
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: PRXScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
            TurnOffAlert -> dataHolder.invokeCommand(DisableAlarm)
            TurnOnAlert -> dataHolder.invokeCommand(EnableAlarm)
        }.exhaustive
    }

    private fun onDisconnectButtonClick() {
        finish()
        dataHolder.clear()
    }
}
