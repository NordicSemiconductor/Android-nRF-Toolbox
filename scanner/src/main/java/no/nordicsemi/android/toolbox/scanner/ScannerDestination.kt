package no.nordicsemi.android.toolbox.scanner

import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.toolbox.scanner.view.ScannerScreen

val ScannerDestinationId = createSimpleDestination("ble-scanner-destination")

val ScannerDestination = defineDestination(ScannerDestinationId) {
    ScannerScreen()
}