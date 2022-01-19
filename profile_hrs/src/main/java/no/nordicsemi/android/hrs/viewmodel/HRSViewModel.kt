package no.nordicsemi.android.hrs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.hrs.data.HRSRepository
import no.nordicsemi.android.hrs.view.DisconnectEvent
import no.nordicsemi.android.hrs.view.DisplayDataState
import no.nordicsemi.android.hrs.view.HRSScreenViewEvent
import no.nordicsemi.android.hrs.view.LoadingState
import no.nordicsemi.android.hrs.view.NavigateUpEvent
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class HRSViewModel @Inject constructor(
    private val repository: HRSRepository,
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

    fun onEvent(event: HRSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
            NavigateUpEvent -> navigationManager.navigateUp()
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
