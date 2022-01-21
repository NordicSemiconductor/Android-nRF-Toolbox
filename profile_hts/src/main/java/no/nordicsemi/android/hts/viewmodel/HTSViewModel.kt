package no.nordicsemi.android.hts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.hts.data.HTSRepository
import no.nordicsemi.android.hts.repository.HTSService
import no.nordicsemi.android.hts.repository.HTS_SERVICE_UUID
import no.nordicsemi.android.hts.view.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class HTSViewModel @Inject constructor(
    private val repository: HTSRepository,
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
        navigationManager.navigateTo(ForwardDestination(ScannerDestinationId), UUIDArgument(ScannerDestinationId, HTS_SERVICE_UUID))

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
            is SuccessDestinationResult -> serviceManager.startService(HTSService::class.java, args.getDevice())
        }.exhaustive
    }

    fun onEvent(event: HTSScreenViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectButtonClick()
            is OnTemperatureUnitSelected -> onTemperatureUnitSelected(event)
        }.exhaustive
    }

    private fun onDisconnectButtonClick() {
        repository.sendDisconnectCommand()
        repository.clear()
    }

    private fun onTemperatureUnitSelected(event: OnTemperatureUnitSelected) {
        repository.setTemperatureUnit(event.value)
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
    }
}
