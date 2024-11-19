package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.azimuthal

import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.QualityIndicator

data class AzimuthMeasurementData(
    val flags: Byte = Byte.MAX_VALUE,
    val quality: QualityIndicator = QualityIndicator.GOOD,
    val address: PeripheralBluetoothAddress = PeripheralBluetoothAddress.TEST,
    val azimuth: Int = 0
)