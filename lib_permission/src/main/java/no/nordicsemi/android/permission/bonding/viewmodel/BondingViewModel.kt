package no.nordicsemi.android.permission.bonding.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.permission.bonding.repository.BondingStateObserver
import no.nordicsemi.android.service.BondingState
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import javax.inject.Inject

@HiltViewModel
class BondingViewModel @Inject constructor(
    private val deviceHolder: SelectedBluetoothDeviceHolder,
    private val bondingStateObserver: BondingStateObserver
) : CloseableViewModel() {

    val state = MutableStateFlow(deviceHolder.getBondingState())

    init {
        bondingStateObserver.events.onEach { event ->
            event.device?.let {
                if (it == deviceHolder.device) {
                    state.tryEmit(event.bondState)
                } else {
                    state.tryEmit(BondingState.NONE)
                }
            } ?: state.tryEmit(event.bondState)
        }.launchIn(viewModelScope)
        bondingStateObserver.startObserving()
    }

    fun bondDevice() {
        deviceHolder.bondDevice()
    }

    override fun onCleared() {
        super.onCleared()
        bondingStateObserver.stopObserving()
    }
}
