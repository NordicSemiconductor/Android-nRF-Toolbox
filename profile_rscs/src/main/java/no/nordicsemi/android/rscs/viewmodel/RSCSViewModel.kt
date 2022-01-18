package no.nordicsemi.android.rscs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.rscs.data.RSCSRepository
import no.nordicsemi.android.rscs.view.DisconnectEvent
import no.nordicsemi.android.rscs.view.DisplayDataState
import no.nordicsemi.android.rscs.view.LoadingState
import no.nordicsemi.android.rscs.view.RSCSState
import no.nordicsemi.android.rscs.view.RSCScreenViewEvent
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class RSCSViewModel @Inject constructor(
    private val repository: RSCSRepository
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> RSCSState(LoadingState)
            BleManagerStatus.OK -> RSCSState(DisplayDataState(data))
            BleManagerStatus.DISCONNECTED -> RSCSState(DisplayDataState(data), false)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, RSCSState(LoadingState))

    fun onEvent(event: RSCScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
        }.exhaustive
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
