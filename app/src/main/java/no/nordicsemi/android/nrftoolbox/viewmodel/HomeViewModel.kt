package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.hts.repository.HTSRepository
import no.nordicsemi.android.nrftoolbox.repository.ActivitySignals
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import javax.inject.Inject

enum class RunningModule {
    CSC,
    HRS,
    HTS,
    RSCS,
    PRX,
    CGM,
    UART
}

data class HomeViewState(
    val runningModule: RunningModule? = null,
    val refreshToggle: Boolean = false,
) {

    fun copyWithRefresh(): HomeViewState {
        return copy(refreshToggle = !refreshToggle)
    }
}

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val navigator: Navigator,
    htsRepository: HTSRepository,
    activitySignals: ActivitySignals,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        htsRepository.isRunning.onEach {
            if (it) {
                _state.value = _state.value.copy(
                    runningModule = RunningModule.HTS,
                )
            } else {
                _state.value = _state.value.copy(
                    runningModule = null,
                )
            }
        }.launchIn(viewModelScope)

        activitySignals.state.onEach {
            _state.value = _state.value.copyWithRefresh()
        }.launchIn(viewModelScope)
    }

    fun startScanning() {
        navigator.navigateTo(ScannerDestinationId)
    }

    fun openProfile(destinationId: DestinationId<Unit, Unit>) {
        navigator.navigateTo(destinationId)
    }

}