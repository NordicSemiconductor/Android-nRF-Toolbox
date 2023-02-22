package no.nordicsemi.android.toolbox.scanner

import android.os.ParcelUuid
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.common.ui.scanner.DeviceSelected
import no.nordicsemi.android.common.ui.scanner.ScannerScreen
import no.nordicsemi.android.common.ui.scanner.ScanningCancelled
import no.nordicsemi.android.kotlin.ble.core.ServerDevice

val ScannerDestinationId = createDestination<ParcelUuid, ServerDevice>("uiscanner-destination")

val ScannerDestination = defineDestination(ScannerDestinationId) {
    val navigationViewModel = hiltViewModel<SimpleNavigationViewModel>()

    val arg = navigationViewModel.parameterOf(ScannerDestinationId)

    ScannerScreen(
        uuid = arg,
        onResult = {
            when (it) {
                is DeviceSelected -> navigationViewModel.navigateUpWithResult(ScannerDestinationId, it.device)
                ScanningCancelled -> navigationViewModel.navigateUp()
            }
        }
    )
}
