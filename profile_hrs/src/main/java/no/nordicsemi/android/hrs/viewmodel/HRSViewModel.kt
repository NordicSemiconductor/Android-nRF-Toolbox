package no.nordicsemi.android.hrs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.hrs.data.HRSRepository
import no.nordicsemi.android.hrs.view.DisconnectEvent
import no.nordicsemi.android.hrs.view.DisplayDataState
import no.nordicsemi.android.hrs.view.HRSScreenViewEvent
import no.nordicsemi.android.hrs.view.HRSState
import no.nordicsemi.android.hrs.view.LoadingState
import no.nordicsemi.android.service.BleManagerStatus
import javax.inject.Inject

@HiltViewModel
internal class HRSViewModel @Inject constructor(
    private val repository: HRSRepository
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> HRSState(LoadingState)
            BleManagerStatus.OK -> HRSState(DisplayDataState(data))
            BleManagerStatus.DISCONNECTED -> HRSState(DisplayDataState(data), false)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, HRSState(LoadingState))

    fun onEvent(event: HRSScreenViewEvent) {
        (event as? DisconnectEvent)?.let {
            onDisconnectButtonClick()
        }
    }

    private fun onDisconnectButtonClick() {
        repository.sendDisconnectCommand()
        repository.clear()
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
    }
}
