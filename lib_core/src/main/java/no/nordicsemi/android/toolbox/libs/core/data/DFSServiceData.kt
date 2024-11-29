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

// TODO: Add two parameter range and measurement section which will be changes with user interaction.
//  For now I am hardcoding the value in the ui, but it will changes later.
//  Which dataclass it should be added needed to be decided.

data class DFSServiceData(
    override val profile: Profile = Profile.DFS,
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val data: Map<PeripheralBluetoothAddress, SensorData> = emptyMap(),
    val isMcpdAvailable: Boolean? = null,
    val isRttAvailable: Boolean? = null,
    val distanceMode: DistanceMode? = null,
    val selectedDevice: PeripheralBluetoothAddress? = null,
    val distanceRange: Range = Range(0,50),
    val availableMeasurementSections: List<MeasurementSection> = emptyList(),
    val selectedMeasurementSection: MeasurementSection? = null
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

fun <T, R> SensorValue<T>?.mapValues(selector: (T) -> R): List<R>? =
    this?.values?.map(selector)

fun <T, R : Comparable<R>> SensorValue<T>?.medianValue(selector: (T) -> R): R? =
    this?.values?.map(selector)?.sorted()?.let { it.getOrNull(it.size / 2) }

fun SensorData.azimuthValues() = azimuth.mapValues { it.azimuth }

fun SensorData.elevationValues() = elevation.mapValues { it.elevation }

fun SensorData.ifftValues() = mcpdDistance.mapValues { it.mcpd.ifft }

fun SensorData.phaseSlopeValues() = mcpdDistance.mapValues { it.mcpd.phaseSlope }

fun SensorData.rssiValues() = mcpdDistance.mapValues { it.mcpd.rssi }

fun SensorData.bestEffortValues() = mcpdDistance.mapValues { it.mcpd.best }

fun SensorData.rttValues() = rttDistance.mapValues { it.rtt.value }

fun SensorData.azimuthValue() = azimuth.medianValue { it.azimuth }

fun SensorData.elevationValue() = elevation.medianValue { it.elevation }

fun SensorData.ifftValue() = mcpdDistance.medianValue { it.mcpd.ifft }

fun SensorData.phaseSlopeValue() = mcpdDistance.medianValue { it.mcpd.phaseSlope }

fun SensorData.rssiValue() = mcpdDistance.medianValue { it.mcpd.rssi }

fun SensorData.bestEffortValue() = mcpdDistance.medianValue { it.mcpd.best }

fun SensorData.rttValue() = rttDistance.medianValue { it.rtt.value }

fun SensorData.distanceValue() = bestEffortValue() ?: rttValue()

fun SensorData.displayAzimuth() = azimuthValue()?.let { "$it°" }

fun SensorData.displayDistance() = distanceValue()?.let { "${it}dm" }

fun SensorData.displayElevation() = elevationValue()?.let { "$it°" }

fun SensorData.isDistanceSettingsAvailable() = mcpdDistance != null || rttDistance != null

fun SensorData.isMcpdSectionAvailable() =
    rttValue() != null || rssiValue() != null || phaseSlopeValue() != null || bestEffortValue() != null

enum class MeasurementSection(val displayName: String) {
    DISTANCE_RTT("RTT"),
    DISTANCE_MCPD_IFFT("IFFT"),
    DISTANCE_MCPD_PHASE_SLOPE("Phase"),
    DISTANCE_MCPD_RSSI("Rssi"),
    DISTANCE_MCPD_BEST("Best"),
}

fun SensorData.availableSections(): List<MeasurementSection> = listOfNotNull(
    this.rttValue()?.let { MeasurementSection.DISTANCE_RTT },
    this.rssiValue()?.let { MeasurementSection.DISTANCE_MCPD_RSSI },
    this.ifftValue()?.let { MeasurementSection.DISTANCE_MCPD_IFFT },
    this.phaseSlopeValue()?.let { MeasurementSection.DISTANCE_MCPD_PHASE_SLOPE },
    this.bestEffortValue()?.let { MeasurementSection.DISTANCE_MCPD_BEST },
)

// Direction Finder Profile Events
data class Range(
    val from: Int,
    val to: Int
)

fun SensorData.selectedMeasurementSectionValues(): List<Int>? =
    when (this.selectedMeasurementSection) {
        MeasurementSection.DISTANCE_RTT -> this.rttValues()
        MeasurementSection.DISTANCE_MCPD_IFFT -> this.ifftValues()
        MeasurementSection.DISTANCE_MCPD_PHASE_SLOPE -> this.phaseSlopeValues()
        MeasurementSection.DISTANCE_MCPD_RSSI -> this.rssiValues()
        MeasurementSection.DISTANCE_MCPD_BEST -> this.bestEffortValues()
        null -> null
    }