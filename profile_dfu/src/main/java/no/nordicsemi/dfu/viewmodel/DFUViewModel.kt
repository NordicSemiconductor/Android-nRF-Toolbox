package no.nordicsemi.dfu.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.dfu.data.Completed
import no.nordicsemi.dfu.data.DFUManager
import no.nordicsemi.dfu.data.DFUProgressManager
import no.nordicsemi.dfu.data.DFURepository
import no.nordicsemi.dfu.data.DFUServiceStatus
import no.nordicsemi.dfu.data.Error
import no.nordicsemi.dfu.data.FileInstallingState
import no.nordicsemi.dfu.data.FileReadyState
import no.nordicsemi.dfu.data.NoFileSelectedState
import no.nordicsemi.dfu.data.ZipFile
import no.nordicsemi.dfu.view.DFUViewEvent
import no.nordicsemi.dfu.view.OnDisconnectButtonClick
import no.nordicsemi.dfu.view.OnInstallButtonClick
import no.nordicsemi.dfu.view.OnPauseButtonClick
import no.nordicsemi.dfu.view.OnStopButtonClick
import no.nordicsemi.dfu.view.OnZipFileSelected
import javax.inject.Inject

@HiltViewModel
internal class DFUViewModel @Inject constructor(
    private val repository: DFURepository,
    private val progressManager: DFUProgressManager,
    private val deviceHolder: SelectedBluetoothDeviceHolder,
    private val dfuManager: DFUManager
) : CloseableViewModel() {

    val state = repository.data.combine(progressManager.status) { state, status ->
        (state as? FileInstallingState)
            ?.run { createInstallingStateWithNewStatus(state, status) }
            ?: state
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NoFileSelectedState())

    init {
        progressManager.registerListener()
    }

    fun onEvent(event: DFUViewEvent) {
        when (event) {
            OnDisconnectButtonClick -> closeScreen()
            OnInstallButtonClick -> {
                dfuManager.install(requireFile())
                repository.install()
            }
            OnPauseButtonClick -> closeScreen()
            OnStopButtonClick -> closeScreen()
            is OnZipFileSelected -> repository.setZipFile(event.file)
        }.exhaustive
    }

    private fun closeScreen() {
        repository.clear()
        deviceHolder.forgetDevice()
        finish()
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
        progressManager.unregisterListener()
    }
}
