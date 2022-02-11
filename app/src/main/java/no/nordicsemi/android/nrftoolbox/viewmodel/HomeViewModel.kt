package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.cgms.data.CGMRepository
import no.nordicsemi.android.csc.data.CSCRepository
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.nrftoolbox.ProfileDestination
import no.nordicsemi.android.nrftoolbox.view.HomeViewState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    cgmRepository: CGMRepository,
    cscRepository: CSCRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        cgmRepository.isRunning.onEach {
            _state.value = _state.value.copy(isCGMModuleRunning = it)
        }.launchIn(viewModelScope)

        cscRepository.isRunning.onEach {
            _state.value = _state.value.copy(isCSCModuleRunning = it)
        }.launchIn(viewModelScope)
    }

    fun openProfile(destination: ProfileDestination) {
        navigationManager.navigateTo(destination.destination.id)
    }
}
