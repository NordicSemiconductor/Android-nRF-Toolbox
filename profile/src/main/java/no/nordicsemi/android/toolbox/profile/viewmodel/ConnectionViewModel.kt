package no.nordicsemi.android.toolbox.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.toolbox.libs.profile.ConnectionProvider
import no.nordicsemi.android.toolbox.libs.profile.ProfileState
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Manager
import javax.inject.Inject

/**
 * This class is responsible for managing the ui states of connection to the peripheral device.
 *
 * @param connectionState The current connection state.
 * @param profileUiState The current profile view state.
 */
data class UiState(
    val connectionState: ConnectionState? = null,
    val profileUiState: ProfileState = ProfileState.Loading,
)

@HiltViewModel
internal class ConnectionViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val connectionProvider: ConnectionProvider
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val peripheralAddress = parameterOf(ProfileDestinationId).deviceAddress
    val peripheral = connectionProvider.findPeripheralByAddress(peripheralAddress)

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Connect to the peripheral.
        if (peripheral != null) {
            connect(peripheral)
        }
        observeConnectionStates()

    }

    private fun observeConnectionStates() {
        // Observe the connection state.
        connectionProvider.apply {
            // Observe the connection state and profile view state.
            combine(connectionState, profileState) { connectionState, profileState ->
                UiState(
                    connectionState = connectionState,
                    profileUiState = profileState
                )
            }.onEach {
                _uiState.value = it
            }.launchIn(viewModelScope)
        }
        // Check the Bluetooth connection status and reestablish the device connection if Bluetooth is reconnected.
        connectionProvider.bleState.drop(1).onEach {
            if (it == Manager.State.POWERED_ON && peripheral?.isDisconnected == true) {
                // Clear the profile manager to start from scratch.
                connectionProvider.clearState()
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
    fun handleProfileDiscovery(profile: ProfileModule?) {
        when (profile) {
            ProfileModule.HTS -> navigateToProfile(HTSDestinationId)

            ProfileModule.CSC,
            ProfileModule.HRS,
            ProfileModule.RSCS,
            ProfileModule.PRX,
            ProfileModule.CGM,
            ProfileModule.UART -> {
                profileNotImplemented()
            }

            null -> {
                connectionProvider.isLoading()
            }
        }
    }

    /**
     * Navigates to the specified profile destination.
     *
     * @param destinationId The ID of the destination to navigate to.
     */
    private fun navigateToProfile(destinationId: DestinationId<Unit, Unit>) {
        navigator.navigateTo(destinationId)
        {
            popUpTo(ProfileDestinationId.toString()) {
                inclusive = true
            }
        }
    }

    /**
     * Connect to the peripheral.
     *
     * @param peripheral The peripheral to connect to.
     */
    private fun connect(peripheral: Peripheral) = viewModelScope.launch {
        connectionProvider.connectAndObservePeripheral(
            peripheral.address,
            autoConnect = false,
            scope = viewModelScope
        )
    }

    /** Disconnect from the peripheral and navigate back. */
    fun onDisconnect() {
        if (peripheral != null) {
            connectionProvider.disconnect(peripheral, viewModelScope)
        }
        connectionProvider.clearState()
        navigator.navigateUp()
    }

    /** This method is called when the profile is not implemented yet. */
    private fun profileNotImplemented() {
        if (peripheral != null) {
            connectionProvider.disconnect(peripheral, viewModelScope)
        }
        connectionProvider.clearState()
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
        connectionProvider.clearState()
    }

}