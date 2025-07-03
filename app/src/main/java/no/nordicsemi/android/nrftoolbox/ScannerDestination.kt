package no.nordicsemi.android.nrftoolbox

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.common.scanner.DeviceSelected
import no.nordicsemi.android.common.scanner.ScannerScreen
import no.nordicsemi.android.common.scanner.ScanningCancelled
import no.nordicsemi.android.common.scanner.data.OnlyNearby
import no.nordicsemi.android.common.scanner.data.OnlyWithNames
import no.nordicsemi.android.common.scanner.rememberFilterState
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.kotlin.ble.client.android.ScanResult

val ScannerDestinationId = createDestination<Unit, ScanResult>("ble-scanner")

val ScannerDestination = defineDestination(ScannerDestinationId) {
    val navigationVM = hiltViewModel<SimpleNavigationViewModel>()

    ScannerScreen(
        cancellable = true,
        state = rememberFilterState(
            dynamicFilters = listOf(
                OnlyNearby(),
                OnlyWithNames(),
            )
        ),
        onResultSelected = {
            when (it) {
                is DeviceSelected -> {
                    navigationVM.navigateTo(ProfileDestinationId, it.scanResult.peripheral.address)
                    {
                        popUpTo(ScannerDestinationId.toString()) {
                            inclusive = true
                        }
                    }
                }

                ScanningCancelled -> {
                    navigationVM.navigateUp()
                }
            }
        }
    )
}
