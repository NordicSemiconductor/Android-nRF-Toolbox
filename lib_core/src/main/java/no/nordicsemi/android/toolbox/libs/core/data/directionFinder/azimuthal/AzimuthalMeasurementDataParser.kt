package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.azimuthal

import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.AddressType
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.QualityIndicator
import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder

class AzimuthalMeasurementDataParser {

    fun parse(
        data: ByteArray,
        byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    ): AzimuthMeasurementData? {
        if (data.size < 10) return null

        var offset = 0
        val flags = data[offset].also { offset++ }
        val qualityIndicator = data.getInt(offset++, IntFormat.UINT8)

        val address = StringBuilder().apply {
            for (i in 0..5) {
                data.getInt(offset++, IntFormat.UINT8).let {
                    insert(0, Integer.toHexString(it))
                    if (i != 5) insert(0, ":")
                }
            }
        }.toString()

        val addressType = data.getInt(offset++, IntFormat.UINT8)

        val azimuth = data.getInt(offset, IntFormat.UINT16, byteOrder).also { offset += 2 }

        return AzimuthMeasurementData(
            flags,
            QualityIndicator.create(qualityIndicator),
            PeripheralBluetoothAddress(AddressType.create(addressType), address),
            azimuth
        )

    }
}
