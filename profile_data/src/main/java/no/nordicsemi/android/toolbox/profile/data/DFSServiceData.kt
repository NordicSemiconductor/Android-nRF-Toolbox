package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.lib.profile.directionFinder.azimuthal.AzimuthMeasurementData
import no.nordicsemi.android.lib.profile.directionFinder.ddf.DDFData
import no.nordicsemi.android.lib.profile.directionFinder.distance.DistanceMode
import no.nordicsemi.android.lib.profile.directionFinder.distance.McpdMeasurementData
import no.nordicsemi.android.lib.profile.directionFinder.distance.RttMeasurementData
import no.nordicsemi.android.lib.profile.directionFinder.elevation.ElevationMeasurementData
import no.nordicsemi.android.lib.profile.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.profile.data.directionFinder.MeasurementSection
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range

private const val MAX_STORED_ITEMS = 5

data class DFSServiceData(
    override val profile: Profile = Profile.DFS,
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val data: Map<PeripheralBluetoothAddress, SensorData> = emptyMap(),
    val ddfFeature: DDFData? = null,
    val selectedDevice: PeripheralBluetoothAddress? = null,
    val distanceRange: Range = Range(0, 50),
) : ProfileServiceData() {

    private val isMcpdAvailable = ddfFeature?.isMcpdAvailable
    private val isRttAvailable = ddfFeature?.isRttAvailable

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
    val distanceMode: DistanceMode? = null,
    val selectedMeasurementSection: MeasurementSection? = null
)

data class SensorValue<T>(
    val values: List<T> = emptyList(),
    val maxItems: Int = MAX_STORED_ITEMS
) {
    fun copyWithNewValue(value: T): SensorValue<T> {
        return SensorValue(values.takeLast(maxItems - 1) + value)
    }
}
