package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.azimuthal

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.AddressType
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.QualityIndicator

class AzimuthalMeasurementDataParser {

    fun parse(data: ByteArray): AzimuthMeasurementData? {
        val bytes = DataByteArray(data)
        if (bytes.size < 10) return null

        var offset = 0
        val flags = bytes.getByte(offset).also { offset++ }
            ?: throw IllegalArgumentException("Byte at offset $offset is null")

        val qualityIndicator = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset).also { offset++ }

        val address = StringBuilder().apply {
            for (i in 0..5) {
                bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++)?.let {
                    insert(0, Integer.toHexString(it))
                    if (i != 5) insert(0, ":")
                }
            }
        }.toString()

        val addressType = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset).also { offset++ }

        val azimuth = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset).also { offset += 2 }

        return AzimuthMeasurementData(
            flags,
            QualityIndicator.create(qualityIndicator!!),
            PeripheralBluetoothAddress(AddressType.create(addressType!!), address),
            azimuth!!
        )

    }
}
