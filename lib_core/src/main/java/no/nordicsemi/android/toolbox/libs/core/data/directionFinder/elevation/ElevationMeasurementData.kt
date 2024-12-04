package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.elevation

import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.QualityIndicator

data class ElevationMeasurementData(
    val flags: Byte = Byte.MAX_VALUE,
    val quality: QualityIndicator = QualityIndicator.GOOD,
    val address: PeripheralBluetoothAddress? = null,
    val elevation: Int = 0
)