package no.nordicsemi.android.nrftoolbox

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.navigation.ForwardDestination
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.navigation.UUIDArgument
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationManager: NavigationManager
) : ViewModel() {

    fun openProfile(destination: ProfileDestination) {
        navigationManager.navigateTo(
            ForwardDestination(destination.destination.id),
            UUIDArgument(destination.destination.id, destination.uuid)
        )
    }
}
