package no.nordicsemi.android.toolbox.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.toolbox.libs.profile.ConnectionProvider
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.android.toolbox.libs.profile.Profile
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.Manager
import javax.inject.Inject

@HiltViewModel
internal class ConnectionViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val connectionProvider: ConnectionProvider
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val peripheralAddress = parameterOf(ProfileDestinationId).deviceAddress
    private val peripheral = connectionProvider.getPeripheral(peripheralAddress)
    val uiState = connectionProvider.uiViewState

    init {
        // Connect to the peripheral.
        if (peripheral != null) {
            connect(peripheral)
        }
        // Check the Bluetooth connection status and reestablish the device connection if Bluetooth is reconnected.
        connectionProvider.state.drop(1).onEach {
            if (it == Manager.State.POWERED_ON && peripheral?.isDisconnected == true) {
                // Clear the profile manager to start from scratch.
                connectionProvider.clear()
                // Reconnect to the peripheral.
                connect(peripheral)
            }
        }.launchIn(viewModelScope)
    }

    /**
     * This method is called when a matching profile is discovered.
     *
     * @param profile The matching profile.
     */
    fun discoveredProfile(profile: Profile?) {
        when (profile) {
            is Profile.HTS -> {
                navigator.navigateTo(HTSDestinationId)
                {
                    popUpTo(ProfileDestinationId.toString()) {
                        inclusive = true
                    }
                }
                // Clear the profile manager states to prevent reconnection.
                connectionProvider.clear()

            }

            null -> {
                connectionProvider.isLoading()
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
        connectionProvider.connectToDevice(peripheral.address, autoConnect = false, scope = viewModelScope)
    }

    /** Disconnect from the peripheral and navigate back. */
    fun onDisconnect() {
        if (peripheral != null) {
            connectionProvider.disconnect(peripheral, viewModelScope)
        }
        connectionProvider.clear()
        navigator.navigateUp()
    }

    /** This method is called when the profile is not implemented yet. */
    private fun profileNotImplemented() {
        if (peripheral != null) {
            connectionProvider.disconnect(peripheral, viewModelScope)
        }
        connectionProvider.clear()
        navigator.navigateUp()
    }

    fun reconnect() {
        if (peripheral != null) {
            connect(peripheral)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clear the profile manager to prevent reconnection.
        connectionProvider.clear()
    }

}