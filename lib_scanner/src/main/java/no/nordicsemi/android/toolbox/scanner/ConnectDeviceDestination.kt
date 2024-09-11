package no.nordicsemi.android.toolbox.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.toolbox.scanner.changed.ClientViewModel
import no.nordicsemi.android.toolbox.scanner.view.ScannerAppBar

val ConnectDeviceDestinationId = createDestination<String, Unit>("connect-device-destination")
val ConnectDeviceDestination = defineDestination(ConnectDeviceDestinationId) {
    val simpleNavigationViewModel: SimpleNavigationViewModel = hiltViewModel()
    ConnectDeviceScreen(simpleNavigationViewModel.parameterOf(ConnectDeviceDestinationId))
}

@Composable
fun ConnectDeviceScreen(peripheral: String) {
    val clientViewModel: ClientViewModel = hiltViewModel()
    val context = LocalContext.current

    LaunchedEffect(peripheral) {
        clientViewModel.connectToPeripheral(context, peripheral)

    }

    Scaffold(
        topBar = {
            ScannerAppBar(title = { Text(peripheral) })
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Connecting to $peripheral")
        }
    }
}
