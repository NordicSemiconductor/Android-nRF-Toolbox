package no.nordicsemi.android.lib.profile.directionFinder.elevation

import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.lib.profile.directionFinder.distance.QualityIndicator

data class ElevationMeasurementData(
    val flags: Byte = Byte.MAX_VALUE,
    val quality: QualityIndicator = QualityIndicator.GOOD,
    val address: PeripheralBluetoothAddress,
    val elevation: Int = 0
)