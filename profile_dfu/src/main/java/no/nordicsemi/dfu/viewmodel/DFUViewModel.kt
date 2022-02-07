package no.nordicsemi.dfu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.navigation.CancelDestinationResult
import no.nordicsemi.android.navigation.DestinationResult
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.navigation.SuccessDestinationResult
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.dfu.data.*
import no.nordicsemi.dfu.repository.DFUService
import no.nordicsemi.dfu.view.*
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class DFUViewModel @Inject constructor(
    private val repository: DFURepository,
    private val progressManager: DFUProgressManager,
    private val dfuManager: DFUManager,
    private val serviceManager: ServiceManager,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private var device: DiscoveredBluetoothDevice? = null

    val state = repository.data.combine(progressManager.status) { state, status ->
        (state as? FileInstallingState)
            ?.run { createInstallingStateWithNewStatus(state, status) }
            ?: state
    }.combine(repository.status) { data, status ->
//        when (status) {
//            BleManagerStatus.CONNECTING -> LoadingState
//            BleManagerStatus.OK,
//            BleManagerStatus.DISCONNECTED -> DisplayDataState(data)
//        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, LoadingState)

    init {
        progressManager.registerListener()
    }

    private fun handleArgs(args: DestinationResult?) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> {
                device = args.getDevice()
                serviceManager.startService(DFUService::class.java, args.getDevice())
            }
            null -> navigationManager.navigateTo(ScannerDestinationId)
        }.exhaustive
    }

    fun onEvent(event: DFUViewEvent) {
        when (event) {
            OnDisconnectButtonClick -> closeScreen()
            OnInstallButtonClick -> {
                dfuManager.install(requireFile(), device!!)
                repository.install()
            }
            OnPauseButtonClick -> closeScreen()
            OnStopButtonClick -> closeScreen()
            is OnZipFileSelected -> repository.setZipFile(event.file, device!!)
        }.exhaustive
    }

    private fun closeScreen() {
        repository.sendNewCommand(DisconnectCommand)
        repository.clear()
    }

    private fun requireFile(): ZipFile {
        return (repository.data.value as FileReadyState).file
    }

    private fun createInstallingStateWithNewStatus(
        state: FileInstallingState,
        status: DFUServiceStatus
    ): FileInstallingState {
        if (status is Error) {
            repository.setError(status.message)
        }
        if (status is Completed) {
            repository.setSuccess()
        }
        return state.copy(status = status)
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
        progressManager.unregisterListener()
    }
}
