package no.nordicsemi.dfu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.navigation.ParcelableArgument
import no.nordicsemi.android.navigation.SuccessDestinationResult
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.dfu.data.Completed
import no.nordicsemi.dfu.data.DFUManager
import no.nordicsemi.dfu.data.DFUProgressManager
import no.nordicsemi.dfu.data.DFURepository
import no.nordicsemi.dfu.data.DFUServiceStatus
import no.nordicsemi.dfu.data.DisconnectCommand
import no.nordicsemi.dfu.data.Error
import no.nordicsemi.dfu.data.FileInstallingState
import no.nordicsemi.dfu.data.FileReadyState
import no.nordicsemi.dfu.data.ZipFile
import no.nordicsemi.dfu.view.DFUState
import no.nordicsemi.dfu.view.DFUViewEvent
import no.nordicsemi.dfu.view.DisplayDataState
import no.nordicsemi.dfu.view.LoadingState
import no.nordicsemi.dfu.view.OnDisconnectButtonClick
import no.nordicsemi.dfu.view.OnInstallButtonClick
import no.nordicsemi.dfu.view.OnPauseButtonClick
import no.nordicsemi.dfu.view.OnStopButtonClick
import no.nordicsemi.dfu.view.OnZipFileSelected
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class DFUViewModel @Inject constructor(
    private val repository: DFURepository,
    private val progressManager: DFUProgressManager,
    private val dfuManager: DFUManager,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val args
        get() = navigationManager.getResult(ScannerDestinationId)
    private val device
        get() = ((args as SuccessDestinationResult).argument as ParcelableArgument).value as DiscoveredBluetoothDevice


    val state = repository.data.combine(progressManager.status) { state, status ->
        (state as? FileInstallingState)
            ?.run { createInstallingStateWithNewStatus(state, status) }
            ?: state
    }.combine(repository.status) { data, status ->
        when (status) {
            BleManagerStatus.CONNECTING -> DFUState(LoadingState)
            BleManagerStatus.OK -> DFUState(DisplayDataState(data))
            BleManagerStatus.DISCONNECTED -> DFUState(DisplayDataState(data), false)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, DFUState(LoadingState))

    init {
        progressManager.registerListener()
    }

    fun onEvent(event: DFUViewEvent) {
        when (event) {
            OnDisconnectButtonClick -> closeScreen()
            OnInstallButtonClick -> {
                dfuManager.install(requireFile(), device)
                repository.install()
            }
            OnPauseButtonClick -> closeScreen()
            OnStopButtonClick -> closeScreen()
            is OnZipFileSelected -> repository.setZipFile(event.file, device)
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
