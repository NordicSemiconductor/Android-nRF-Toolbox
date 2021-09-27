package no.nordicsemi.android.hrs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import no.nordicsemi.android.hrs.events.HRSAggregatedData
import no.nordicsemi.android.hrs.service.HRSDataBroadcast
import no.nordicsemi.android.hrs.view.DisconnectEvent
import no.nordicsemi.android.hrs.view.HRSScreenViewEvent
import javax.inject.Inject

@HiltViewModel
internal class HRSViewModel @Inject constructor(
    private val localBroadcast: HRSDataBroadcast
) : ViewModel() {

    val state = MutableStateFlow(HRSViewState())

    init {
        localBroadcast.events.onEach {
            withContext(Dispatchers.Main) { consumeEvent(it) }
        }.launchIn(viewModelScope)
    }

    private fun consumeEvent(event: HRSAggregatedData) {
        state.value = state.value.copy(
            points = event.heartRates,
            batteryLevel = event.batteryLevel,
            sensorLocation = event.sensorLocation
        )
    }

    fun onEvent(event: HRSScreenViewEvent) {
        (event as? DisconnectEvent)?.let {
            onDisconnectButtonClick()
        }
    }

    private fun onDisconnectButtonClick() {
        state.tryEmit(state.value.copy(isScreenActive = false))
    }
}
