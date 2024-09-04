package no.nordicsemi.android.toolbox.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.repository.ConnectionRepository
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.android.toolbox.scanner.view.ScannerAppBar
import javax.inject.Inject

val ConnectDeviceDestinationId = createDestination<String, Unit>("connect-device-destination")
val ConnectDeviceDestination = defineDestination(ConnectDeviceDestinationId) {
    ConnectDeviceScreen()
}

@Composable
fun ConnectDeviceScreen() {
    val viewModel: ConnectViewModel = hiltViewModel()
    val state by viewModel.peripheralState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ScannerAppBar(title = { Text(viewModel.peripheral) })
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text("Connecting to ${viewModel.peripheral}")
        }
    }

}

@HiltViewModel
internal class ConnectViewModel @Inject constructor(
    private val navigator: Navigator,
    private val repository: ConnectionRepository,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(savedStateHandle = savedStateHandle, navigator = navigator) {
    val peripheral = parameterOf(ConnectDeviceDestinationId)
    val peripheralState = repository.peripheralState

    init {
        repository.connectAndLunchService(peripheral)
        navigateToProfileScreen()
    }

    private fun navigateToProfileScreen() {
        viewModelScope.launch {
            repository.connectedDevice.collect { devices ->
                // Check if the peripheral is in the connected devices
                // Explicitly define the key and value types to help with type inference
                val handlers: List<ProfileHandler>? = devices.entries
                    .firstOrNull { it.key.address == peripheral }
                    ?.value

                // Once handlers are found, invoke getData
                handlers?.let { profileHandlers ->
                    if (profileHandlers.find { it.profileModule == ProfileModule.HTS } != null) {
                        navigator.navigateTo(HTSDestinationId, peripheral)
                    }
                }
            }
        }

    }
}