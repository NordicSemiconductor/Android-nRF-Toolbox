package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrftoolbox.repository.ActivitySignals
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import javax.inject.Inject

data class HomeViewState(
    val profileModule: ProfileModule? = null,
    val refreshToggle: Boolean = false,
) {
    fun toggleRefresh(): HomeViewState = copy(refreshToggle = !refreshToggle)
}

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val navigator: Navigator,
    activitySignals: ActivitySignals,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        activitySignals.state.onEach {
            _state.update { currentState ->
                currentState.toggleRefresh()
            }
        }.launchIn(viewModelScope)
    }

    fun startScanning() {
        navigator.navigateTo(ScannerDestinationId)
    }

    fun openProfile(destinationId: DestinationId<Unit, Unit>) {
        navigator.navigateTo(destinationId)
    }

}