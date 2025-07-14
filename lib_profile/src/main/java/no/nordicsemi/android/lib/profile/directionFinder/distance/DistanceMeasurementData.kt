package no.nordicsemi.android.lib.profile.directionFinder.distance

import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress

sealed interface DistanceMeasurementData {
    val flags: Byte
    val quality: QualityIndicator
    val address: PeripheralBluetoothAddress
}

data class McpdMeasurementData(
    override val flags: Byte = Byte.MAX_VALUE,
    override val quality: QualityIndicator = QualityIndicator.GOOD,
    override val address: PeripheralBluetoothAddress,
    val mcpd: MCPDEstimate = MCPDEstimate()
) : DistanceMeasurementData

data class RttMeasurementData(
    override val flags: Byte = Byte.MAX_VALUE,
    override val quality: QualityIndicator = QualityIndicator.GOOD,
    override val address: PeripheralBluetoothAddress,
    val rtt: RTTEstimate = RTTEstimate()
) : DistanceMeasurementData

data class MCPDEstimate(
    val ifft: Int = 0,
    val phaseSlope: Int = 0,
    val rssi: Int = 0,
    val best: Int = 0
) {

    operator fun plus(value: Int): MCPDEstimate {
        return MCPDEstimate(
            ifft + value,
            phaseSlope + value,
            rssi + value,
            best + value
        )
    }
}

data class RTTEstimate(
    val value: Int = 0
) {
    operator fun inc(): RTTEstimate {
        return RTTEstimate(value + 1)
    }

    operator fun plus(value: Int): RTTEstimate {
        return RTTEstimate(this.value + value)
    }
}
