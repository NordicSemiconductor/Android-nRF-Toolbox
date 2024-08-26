package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.hts.repository.HTSRepository
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val navigator: Navigator,
    private val htsRepository: HTSRepository,
) : ViewModel() {

    fun startScanning() {
        htsRepository.isRunning.onEach {
            if (it) {
                navigator.navigateTo(HTSDestinationId)
            } else navigator.navigateTo(ScannerDestinationId)
        }.launchIn(viewModelScope)
    }

    fun cancel() {
        viewModelScope.cancel()
        onCleared()
    }

}