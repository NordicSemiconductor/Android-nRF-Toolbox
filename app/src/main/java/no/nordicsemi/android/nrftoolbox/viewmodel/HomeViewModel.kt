package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.cgms.repository.CGMRepository
import no.nordicsemi.android.csc.repository.CSCRepository
import no.nordicsemi.android.hrs.service.HRSRepository
import no.nordicsemi.android.hts.repository.HTSRepository
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.nrftoolbox.ProfileDestination
import no.nordicsemi.android.nrftoolbox.view.HomeViewState
import no.nordicsemi.android.prx.repository.PRXRepository
import no.nordicsemi.android.rscs.repository.RSCSRepository
import no.nordicsemi.android.uart.repository.UARTRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    cgmRepository: CGMRepository,
    cscRepository: CSCRepository,
    hrsRepository: HRSRepository,
    htsRepository: HTSRepository,
    prxRepository: PRXRepository,
    rscsRepository: RSCSRepository,
    uartRepository: UARTRepository,
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

        hrsRepository.isRunning.onEach {
            _state.value = _state.value.copy(isHRSModuleRunning = it)
        }.launchIn(viewModelScope)

        htsRepository.isRunning.onEach {
            _state.value = _state.value.copy(isHTSModuleRunning = it)
        }.launchIn(viewModelScope)

        prxRepository.isRunning.onEach {
            _state.value = _state.value.copy(isPRXModuleRunning = it)
        }.launchIn(viewModelScope)

        rscsRepository.isRunning.onEach {
            _state.value = _state.value.copy(isRSCSModuleRunning = it)
        }.launchIn(viewModelScope)

        uartRepository.isRunning.onEach {
            _state.value = _state.value.copy(isUARTModuleRunning = it)
        }.launchIn(viewModelScope)
    }

    fun openProfile(destination: ProfileDestination) {
        navigationManager.navigateTo(destination.destination.id)
    }
}
