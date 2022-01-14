package no.nordicsemi.android.nrftoolbox

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceCloseResult
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceFlowStatus
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceSuccessResult
import no.nordicsemi.ui.scanner.ui.exhaustive
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deviceHolder: SelectedBluetoothDeviceHolder
) : ViewModel() {

    private val _destination = MutableStateFlow<NavDestination>(HomeDestination)
    val destination = _destination.asStateFlow()

    var profile: Profile? = null //to pass argument between nav destinations

    fun onScannerFlowResult(status: FindDeviceFlowStatus) {
        when (status) {
            FindDeviceCloseResult -> navigateUp()
            is FindDeviceSuccessResult -> onDeviceSelected(status)
        }.exhaustive
    }

    fun openProfile(profile: Profile) {
        this.profile = profile
        _destination.value = ScannerDestination(profile)
    }

    fun navigateUp() {
        val currentDestination = _destination.value
        when (currentDestination) {
            FinishDestination -> FinishDestination
            HomeDestination -> FinishDestination
            is ProfileDestination -> HomeDestination
            is ScannerDestination -> HomeDestination
        }.exhaustive
    }

    private fun onDeviceSelected(result: FindDeviceSuccessResult) {
        val profile = requireNotNull(profile)
        deviceHolder.attachDevice(result.device)
        _destination.value = ProfileDestination(profile.toNavigationId(), profile.isPairingRequired)
    }
}
