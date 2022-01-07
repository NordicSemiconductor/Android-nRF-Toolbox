package no.nordicsemi.dfu.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.dfu.data.DFUFile
import no.nordicsemi.dfu.data.DFUFileManager
import no.nordicsemi.dfu.data.DFUManager
import no.nordicsemi.dfu.data.DFUProgressManager
import no.nordicsemi.dfu.data.DFURepository
import no.nordicsemi.dfu.data.FileInstallingState
import no.nordicsemi.dfu.data.FileReadyState
import no.nordicsemi.dfu.view.DFUViewEvent
import no.nordicsemi.dfu.view.OnDatFileSelected
import no.nordicsemi.dfu.view.OnDisconnectButtonClick
import no.nordicsemi.dfu.view.OnHexFileSelected
import no.nordicsemi.dfu.view.OnInstallButtonClick
import no.nordicsemi.dfu.view.OnPauseButtonClick
import no.nordicsemi.dfu.view.OnStopButtonClick
import no.nordicsemi.dfu.view.OnZipFileSelected
import javax.inject.Inject

@HiltViewModel
internal class DFUViewModel @Inject constructor(
    private val repository: DFURepository,
    private val progressManager: DFUProgressManager,
    private val dfuManager: DFUManager,
    private val fileManger: DFUFileManager
) : CloseableViewModel() {

    val state = repository.data.combine(progressManager.status) { state, status ->
        (state as? FileInstallingState)?.run {
            state.copy(status = status)
        } ?: state
    }

    init {
        progressManager.registerListener()
    }

    fun onEvent(event: DFUViewEvent) {
        when (event) {
            OnDisconnectButtonClick -> finish()
            OnInstallButtonClick -> {
                dfuManager.install(requireFile())
                repository.install()
            }
            OnPauseButtonClick -> finish()
            OnStopButtonClick -> finish()
            is OnHexFileSelected -> repository.
            is OnZipFileSelected -> TODO()
            is OnDatFileSelected -> TODO()
        }.exhaustive
    }

    private fun requireFile(): DFUFile {
        return (repository.data.value as FileReadyState).file
    }

    override fun onCleared() {
        super.onCleared()
        progressManager.unregisterListener()
    }
}
