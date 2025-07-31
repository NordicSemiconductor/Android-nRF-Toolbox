package no.nordicsemi.android.lib.profile.directionFinder.elevation

import no.nordicsemi.android.lib.profile.directionFinder.AddressType
import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.lib.profile.directionFinder.distance.QualityIndicator
import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getInt

class ElevationMeasurementDataParser {

    fun parse(data: ByteArray): ElevationMeasurementData? {
        if (data.size < 10) return null

        var offset = 0
        val flags = data[offset].also { offset++ }

        val qualityIndicator = data.getInt(offset++, IntFormat.UINT8)

        val address = StringBuilder().apply {
            for (i in 0..5) {
                data.getInt(offset++, IntFormat.UINT8).let {
                    insert(0, Integer.toHexString(it).padStart(2, '0'))
                    if (i != 5) insert(0, ":")
                }
            }
        }.toString()

        val addressType = data.getInt(offset++, IntFormat.UINT8)

        val elevation = data.getInt(offset++, IntFormat.INT8)

        return ElevationMeasurementData(
            flags,
            QualityIndicator.create(qualityIndicator),
            PeripheralBluetoothAddress(AddressType.create(addressType), address),
            elevation
        )
    }
}
