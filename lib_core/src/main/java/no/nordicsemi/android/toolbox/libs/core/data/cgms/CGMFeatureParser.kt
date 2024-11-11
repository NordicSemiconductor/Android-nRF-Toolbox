package no.nordicsemi.android.toolbox.libs.core.data.cgms

import android.annotation.SuppressLint
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMFeatures
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMFeaturesEnvelope
import no.nordicsemi.android.toolbox.libs.core.data.common.CRC16

object CGMFeatureParser {

    fun parse(data: ByteArray): CGMFeaturesEnvelope? {
        val bytes = DataByteArray(data)
        if (bytes.size != 6) {
            return null
        }

        val featuresValue: Int = bytes.getIntValue(IntFormat.FORMAT_UINT24_LE, 0) ?: return null
        val typeAndSampleLocation: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 3) ?: return null
        val expectedCrc: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 4) ?: return null

        val features = CGMFeatures(featuresValue)
        if (features.e2eCrcSupported) {
            val actualCrc: Int = CRC16.MCRF4XX(bytes.value, 0, 4)
            if (actualCrc != expectedCrc) {
                return null
            }
        } else {
            // If the device doesn't support E2E-safety the value of the field shall be set to 0xFFFF.
            if (expectedCrc != 0xFFFF) {
                return null
            }
        }

        @SuppressLint("WrongConstant") val type = typeAndSampleLocation and 0x0F // least significant nibble

        val sampleLocation = typeAndSampleLocation shr 4 // most significant nibble

        return CGMFeaturesEnvelope(features, type, sampleLocation, features.e2eCrcSupported, features.e2eCrcSupported)
    }
}
