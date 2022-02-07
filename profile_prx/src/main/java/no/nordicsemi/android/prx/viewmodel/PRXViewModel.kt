package no.nordicsemi.android.prx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.prx.data.DisableAlarm
import no.nordicsemi.android.prx.data.Disconnect
import no.nordicsemi.android.prx.data.EnableAlarm
import no.nordicsemi.android.prx.data.PRXRepository
import no.nordicsemi.android.prx.repository.PRXService
import no.nordicsemi.android.prx.repository.PRX_SERVICE_UUID
import no.nordicsemi.android.prx.view.*
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class PRXViewModel @Inject constructor(
    private val repository: PRXRepository,
    private val serviceManager: ServiceManager,
    private val navigationManager: NavigationManager
) : ViewModel() {

    val state = repository.data.combine(repository.status) { data, status ->
//        when (status) {
//            BleManagerStatus.CONNECTING -> LoadingState
//            BleManagerStatus.OK,
//            BleManagerStatus.DISCONNECTED -> DisplayDataState(data)
//        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, LoadingState)

    init {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(PRX_SERVICE_UUID))

        navigationManager.recentResult.onEach {
            if (it.destinationId == ScannerDestinationId) {
                handleArgs(it)
            }
        }.launchIn(viewModelScope)

        repository.status.onEach {
            if (it == BleManagerStatus.DISCONNECTED) {
                navigationManager.navigateUp()
            }
        }.launchIn(viewModelScope)
    }

    private fun handleArgs(args: DestinationResult) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> serviceManager.startService(PRXService::class.java, args.getDevice())
        }.exhaustive
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
