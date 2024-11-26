package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.azimuthal.AzimuthMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.DistanceMode
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.McpdMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.RttMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.elevation.ElevationMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus

private const val MAX_STORED_ITEMS = 5

data class DFSServiceData(
    override val profile: Profile = Profile.DFS,
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val data: Map<PeripheralBluetoothAddress, SensorData> = emptyMap(),
    val isMcpdAvailable: Boolean? = null,
    val isRttAvailable: Boolean? = null,
    val distanceMode: DistanceMode? = null,
) : ProfileServiceData() {

    fun isDistanceAvailable(): Boolean {
        return isMcpdAvailable == true || isRttAvailable == true
    }

    fun isDistanceAvailabilityChecked(): Boolean {
        return isMcpdAvailable != null || isRttAvailable != null
    }

    fun isDoubleModeAvailable(): Boolean {
        return isMcpdAvailable == true && isRttAvailable == true
    }
}

data class SensorData(
    val azimuth: SensorValue<AzimuthMeasurementData>? = null,
    val elevation: SensorValue<ElevationMeasurementData>? = null,
    val mcpdDistance: SensorValue<McpdMeasurementData>? = null,
    val rttDistance: SensorValue<RttMeasurementData>? = null,
    val distanceMode: DistanceMode? = null
)

data class SensorValue<T>(
    val values: List<T> = emptyList(),
    val maxItems: Int = MAX_STORED_ITEMS
) {
    fun copyWithNewValue(value: T): SensorValue<T> {
        return SensorValue(values.takeLast(maxItems - 1) + value)
    }
}
