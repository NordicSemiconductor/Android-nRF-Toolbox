package no.nordicsemi.android.toolbox.profile.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.android.toolbox.profile.repository.ProfileManager
import no.nordicsemi.android.ui.view.MockRemoteService
import no.nordicsemi.android.ui.view.Profile
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val profileManager: ProfileManager,
    @ApplicationContext private val context: Context
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val peripheral = parameterOf(ProfileDestinationId).peripheral
    val uiState = profileManager.uiViewState

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
                profileManager.isLoading()
            }

            else -> {
                profileNotImplemented()
            }
        }
    }

    /**
     * Connect to the peripheral.
     *
     * @param peripheral The peripheral to connect to.
     */
    private fun connect(peripheral: Peripheral) = viewModelScope.launch {
        profileManager.connect(peripheral)
    }

    /** Disconnect from the peripheral and navigate back. */
    fun onDisconnect() {
        profileManager.disconnect(peripheral)
        navigator.navigateUp()
    }

    /** This method is called when the profile is not implemented yet. */
    private fun profileNotImplemented() {
        Toast.makeText(context, "Profile not implemented yet.", Toast.LENGTH_SHORT).show()
        profileManager.disconnect(peripheral)
        navigator.navigateUp()
    }

}