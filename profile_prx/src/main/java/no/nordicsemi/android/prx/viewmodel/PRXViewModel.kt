package no.nordicsemi.android.prx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.prx.data.DisableAlarm
import no.nordicsemi.android.prx.data.Disconnect
import no.nordicsemi.android.prx.data.EnableAlarm
import no.nordicsemi.android.prx.data.PRXRepository
import no.nordicsemi.android.prx.view.DisconnectEvent
import no.nordicsemi.android.prx.view.DisplayDataState
import no.nordicsemi.android.prx.view.LoadingState
import no.nordicsemi.android.prx.view.PRXScreenViewEvent
import no.nordicsemi.android.prx.view.TurnOffAlert
import no.nordicsemi.android.prx.view.TurnOnAlert
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class PRXViewModel @Inject constructor(
    private val repository: PRXRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> LoadingState
            BleManagerStatus.OK,
            BleManagerStatus.DISCONNECTED -> DisplayDataState(data)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, LoadingState)

    init {
        repository.status.onEach {
            if (it == BleManagerStatus.DISCONNECTED) {
                navigationManager.navigateUp()
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: PRXScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
            TurnOffAlert -> repository.invokeCommand(DisableAlarm)
            TurnOnAlert -> repository.invokeCommand(EnableAlarm)
        }.exhaustive
    }

    private fun onDisconnectButtonClick() {
        repository.invokeCommand(Disconnect)
        repository.clear()
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
    }
}
