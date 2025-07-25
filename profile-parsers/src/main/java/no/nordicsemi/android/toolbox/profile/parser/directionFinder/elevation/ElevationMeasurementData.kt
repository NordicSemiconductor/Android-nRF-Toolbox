package no.nordicsemi.android.toolbox.profile.parser.directionFinder.elevation

import no.nordicsemi.android.toolbox.profile.parser.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.distance.QualityIndicator

data class ElevationMeasurementData(
    val flags: Byte = Byte.MAX_VALUE,
    val quality: QualityIndicator = QualityIndicator.GOOD,
    val address: PeripheralBluetoothAddress,
    val elevation: Int = 0
)