package no.nordicsemi.android.toolbox.libs.profile

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

val DeviceConnectionDestinationId = createDestination<String, Unit>("connect-device-destination")
val DeviceConnectionDestination = defineDestination(DeviceConnectionDestinationId) {
    val simpleNavigationViewModel: SimpleNavigationViewModel = hiltViewModel()
    val deviceAddress = simpleNavigationViewModel.parameterOf(DeviceConnectionDestinationId)
    DeviceConnectionScreen(deviceAddress)
}
