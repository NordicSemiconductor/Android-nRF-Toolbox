package no.nordicsemi.android.toolbox.scanner

import android.os.ParcelUuid
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.ui.scanner.DeviceSelected
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScannerScreen
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScanningCancelled

val ScannerDestinationId = createDestination<ParcelUuid, SelectedDevice>("uiscanner-destination")

val ScannerDestination = defineDestination(ScannerDestinationId) {
    val navigationViewModel = hiltViewModel<SimpleNavigationViewModel>()

    val arg = navigationViewModel.parameterOf(ScannerDestinationId)

    ScannerScreen(
        uuid = arg,
        onResult = {
            when (it) {
                is DeviceSelected -> navigationViewModel
                    .navigateUpWithResult(
                        ScannerDestinationId,
                        SelectedDevice(it.scanResults.device, it.scanResults.advertisedName)
                    )
                ScanningCancelled -> navigationViewModel.navigateUp()
            }
        }
    )
}

class SelectedDevice(val device: ServerDevice, advertisedName: String?) {
    val name: String? = advertisedName?.ifBlank { device.name }
}