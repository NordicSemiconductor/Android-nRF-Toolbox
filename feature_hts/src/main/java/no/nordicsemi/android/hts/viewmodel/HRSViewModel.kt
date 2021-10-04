package no.nordicsemi.android.hts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import no.nordicsemi.android.hts.data.HTSData
import no.nordicsemi.android.hts.service.HTSDataBroadcast
import no.nordicsemi.android.hts.view.DisconnectEvent
import no.nordicsemi.android.hts.view.HTSScreenViewEvent
import javax.inject.Inject

@HiltViewModel
internal class HTSViewModel @Inject constructor(
    private val localBroadcast: HTSDataBroadcast
) : ViewModel() {

    val state = MutableStateFlow(HTSData())

    init {
        localBroadcast.events.onEach {
            withContext(Dispatchers.Main) { consumeEvent(it) }
        }.launchIn(viewModelScope)
    }

    private fun consumeEvent(event: HTSData) {
        state.value = state.value.copy(

            batteryLevel = event.batteryLevel,
            sensorLocation = event.sensorLocation
        )
    }

    fun onEvent(event: HTSScreenViewEvent) {
        (event as? DisconnectEvent)?.let {
            onDisconnectButtonClick()
        }
    }

    private fun onDisconnectButtonClick() {
        state.tryEmit(state.value.copy(isScreenActive = false))
    }
}
