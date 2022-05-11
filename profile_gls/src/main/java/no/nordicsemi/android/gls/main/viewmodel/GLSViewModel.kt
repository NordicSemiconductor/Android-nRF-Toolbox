package no.nordicsemi.android.gls.main.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.Profile
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.gls.GlsDetailsDestinationId
import no.nordicsemi.android.gls.repository.GLSRepository
import no.nordicsemi.android.gls.data.GLS_SERVICE_UUID
import no.nordicsemi.android.gls.main.view.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.service.SuccessResult
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class GLSViewModel @Inject constructor(
    private val repository: GLSRepository,
    private val navigationManager: NavigationManager,
    private val analytics: AppAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow<GLSViewState>(NoDeviceState)
    val state = _state.asStateFlow()

    init {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(GLS_SERVICE_UUID))

        navigationManager.recentResult.onEach {
            if (it.destinationId == ScannerDestinationId) {
                handleArgs(it)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleArgs(args: DestinationResult) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> connectDevice(args.getDevice())
        }.exhaustive
    }

    fun onEvent(event: GLSScreenViewEvent) {
        when (event) {
            OpenLoggerEvent -> repository.openLogger()
            DisconnectEvent -> navigationManager.navigateUp()
            is OnWorkingModeSelected -> repository.requestMode(event.workingMode)
            is OnGLSRecordClick -> navigationManager.navigateTo(GlsDetailsDestinationId, AnyArgument(event.record))
            DisconnectEvent -> navigationManager.navigateUp()
        }.exhaustive
    }

    private fun connectDevice(device: DiscoveredBluetoothDevice) {
        repository.downloadData(viewModelScope, device).onEach {
            _state.value = WorkingState(it)

            (it as? SuccessResult)?.let {
                analytics.logEvent(ProfileConnectedEvent(Profile.GLS))
            }
        }.launchIn(viewModelScope)
    }
}
