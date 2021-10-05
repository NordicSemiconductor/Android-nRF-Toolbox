package no.nordicsemi.android.hts.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.hts.data.HTSDataHolder
import no.nordicsemi.android.hts.view.DisconnectEvent
import no.nordicsemi.android.hts.view.HTSScreenViewEvent
import no.nordicsemi.android.hts.view.OnTemperatureUnitSelected
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class HTSViewModel @Inject constructor(
    private val dataHolder: HTSDataHolder
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: HTSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
            is OnTemperatureUnitSelected -> onTemperatureUnitSelected(event)
        }.exhaustive
    }

    private fun onDisconnectButtonClick() {
        finish()
        dataHolder.clear()
    }

    private fun onTemperatureUnitSelected(event: OnTemperatureUnitSelected) {
        dataHolder.setTemperatureUnit(event.value)
    }
}
