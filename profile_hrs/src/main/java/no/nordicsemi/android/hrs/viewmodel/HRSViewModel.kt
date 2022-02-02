package no.nordicsemi.android.hrs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.hrs.data.HRSRepository
import no.nordicsemi.android.hrs.service.HRSService
import no.nordicsemi.android.hrs.service.HRS_SERVICE_UUID
import no.nordicsemi.android.hrs.view.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class HRSViewModel @Inject constructor(
    private val repository: HRSRepository,
    private val serviceManager: ServiceManager,
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
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(HRS_SERVICE_UUID))

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
            is SuccessDestinationResult -> serviceManager.startService(HRSService::class.java, args.getDevice())
        }.exhaustive
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
