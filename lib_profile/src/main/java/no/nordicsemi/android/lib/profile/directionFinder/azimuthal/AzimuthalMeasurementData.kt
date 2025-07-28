package no.nordicsemi.android.lib.profile.directionFinder.azimuthal

import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.lib.profile.directionFinder.distance.QualityIndicator

/**
 * Azimuth represents the horizontal direction of a signal source relative to a
 * receiver or reference point. Represents the horizontal direction (angle on a flat plane)
 * Example: AzimuthMeasurementData(
 *   flags=0,
 *   quality=GOOD,
 *   address=PeripheralBluetoothAddress(type=RANDOM, address=aa:bb:cc:dd:ee:ff),
 *   azimuth=156
 * ) here azimuth = 156° indicates that the detected device is 156° clockwise from the reference direction (e.g., true north or some defined zero-point).
 * The quality=GOOD suggests the measurement is reliable. Azimuthal data in devices like yours is calculated using signal phase differences and other electronic measurements.
 */
data class AzimuthMeasurementData(
    val flags: Byte = Byte.MAX_VALUE,
    val quality: QualityIndicator = QualityIndicator.GOOD,
    val address: PeripheralBluetoothAddress?=null,
    val azimuth: Int = 0
)