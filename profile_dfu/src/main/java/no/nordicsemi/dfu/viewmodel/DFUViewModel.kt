package no.nordicsemi.dfu.viewmodel

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.dfu.data.DFURepository
import no.nordicsemi.dfu.view.*
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class DFUViewModel @Inject constructor(
    private val repository: DFURepository,
) : CloseableViewModel() {

    val state = repository.data

    fun onEvent(event: DFUViewEvent) {
        when (event) {
            OnDisconnectButtonClick -> finish()
            is OnFileSelected -> repository.initFile(createFile(event.uri))
            OnInstallButtonClick -> repository.install()
            OnPauseButtonClick -> finish()
            OnStopButtonClick -> finish()
        }.exhaustive
    }

    private fun createFile(uri: Uri): File {
        return File(requireNotNull(uri.path))
    }
}
