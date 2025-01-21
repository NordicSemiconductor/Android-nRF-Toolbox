package no.nordicsemi.android.toolbox.libs.core.data.cgms

import android.annotation.SuppressLint
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMFeatures
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMFeaturesEnvelope
import no.nordicsemi.android.toolbox.libs.core.data.common.CRC16
import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder

object CGMFeatureParser {

    fun parse(
        data: ByteArray,
        byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    ): CGMFeaturesEnvelope? {
        if (data.size != 6) return null

        val featuresValue: Int =
            data.getInt(0, IntFormat.UINT24, byteOrder)
        val typeAndSampleLocation: Int = data.getInt(3, IntFormat.UINT8)
        val expectedCrc: Int = data.getInt(4, IntFormat.UINT16, byteOrder)

        val features = CGMFeatures(featuresValue)
        if (features.e2eCrcSupported) {
            val actualCrc: Int = CRC16.MCRF4XX(data, 0, 4)
            if (actualCrc != expectedCrc) return null
        } else {
            // If the device doesn't support E2E-safety the value of the field shall be set to 0xFFFF.
            if (expectedCrc != 0xFFFF) return null
        }

        @SuppressLint("WrongConstant") val type =
            typeAndSampleLocation and 0x0F // least significant nibble

        val sampleLocation = typeAndSampleLocation shr 4 // most significant nibble

        return CGMFeaturesEnvelope(
            features,
            type,
            sampleLocation,
            features.e2eCrcSupported,
            features.e2eCrcSupported
        )
    }
}
