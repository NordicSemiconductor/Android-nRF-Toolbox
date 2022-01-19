package no.nordicsemi.android.nrftoolbox

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.navigation.ForwardDestination
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.navigation.UUIDArgument
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deviceHolder: SelectedBluetoothDeviceHolder,
    private val navigationManager: NavigationManager
) : ViewModel() {

    fun openProfile(destination: ProfileDestination) {
        navigationManager.navigateTo(
            ForwardDestination(destination.destination.id),
            UUIDArgument(destination.uuid)
        )
    }
}
