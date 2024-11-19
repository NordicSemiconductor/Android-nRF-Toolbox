package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance

import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress

sealed class DistanceMeasurementData

data class McpdMeasurementData(
    val flags: Byte = Byte.MAX_VALUE,
    val quality: QualityIndicator = QualityIndicator.GOOD,
    val address: PeripheralBluetoothAddress = PeripheralBluetoothAddress.TEST,
    val mcpd: MCPDEstimate = MCPDEstimate()
) : DistanceMeasurementData()

data class RttMeasurementData(
    val flags: Byte = Byte.MAX_VALUE,
    val quality: QualityIndicator = QualityIndicator.GOOD,
    val address: PeripheralBluetoothAddress = PeripheralBluetoothAddress.TEST,
    val rtt: RTTEstimate = RTTEstimate()
) : DistanceMeasurementData()

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
