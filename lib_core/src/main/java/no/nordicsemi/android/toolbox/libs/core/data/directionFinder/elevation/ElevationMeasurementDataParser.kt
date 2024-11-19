package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.elevation

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.AddressType
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.QualityIndicator

class ElevationMeasurementDataParser {

    fun parse(data: ByteArray): ElevationMeasurementData? {
        val bytes = DataByteArray(data)
        if (bytes.size < 10) {
            return null
        }

        var offset = 0
        val flags = bytes.getByte(offset).also { offset++ }
            ?: throw IllegalArgumentException("Byte at offset $offset is null")

        val qualityIndicator = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset).also { offset++ }

        val address = StringBuilder().apply {
            for (i in 0..5) {
                bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++)?.let {
                    insert(0, Integer.toHexString(it).padStart(2, '0'))
                    if (i != 5) insert(0, ":")
                }
            }
        }.toString()

        val addressType = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset).also { offset++ }

        val elevation = bytes.getIntValue(IntFormat.FORMAT_SINT8, offset).also { offset++ }

        return ElevationMeasurementData(
            flags,
            QualityIndicator.create(qualityIndicator!!),
            PeripheralBluetoothAddress(AddressType.create(addressType!!), address),
            elevation!!
        )
    }
}
