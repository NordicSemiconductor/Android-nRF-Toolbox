package no.nordicsemi.android.toolbox.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.android.toolbox.profile.repository.ConnectionManager
import no.nordicsemi.android.ui.view.Profile
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val connectionManager: ConnectionManager,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val peripheral = parameterOf(ProfileDestinationId).peripheral
    val connectionState = connectionManager.connectionState

    init {
        connect(peripheral)
    }

    /**
     * This method is called when a matching profile is discovered.
     *
     * @param profile The matching profile.
     */
    fun discoveredProfile(profile: Profile?) {
        when (profile) {
            is Profile.HTS -> {
                val args = MockRemoteService(
                    serviceData = profile.remoteService.serviceData,
                    peripheral = peripheral,
                )
                navigator.navigateTo(HTSDestinationId, Profile.HTS(args))
            }

                null -> {
                    connectionManager.isLoading()
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun connect(peripheral: Peripheral) = viewModelScope.launch {
        connectionManager.connect(peripheral)
    }

    fun onDisconnect() {
        connectionManager.disconnect(peripheral)
        navigator.navigateUp()
    }

}