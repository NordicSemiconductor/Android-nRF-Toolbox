package no.nordicsemi.android.toolbox.libs.core.data.gls

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.NumberOfRecordsData
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RecordAccessControlPointData
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.ResponseData
import no.nordicsemi.android.toolbox.libs.core.data.racp.RACPOpCode
import no.nordicsemi.android.toolbox.libs.core.data.racp.RACPResponseCode

object RecordAccessControlPointParser {
    private const val OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5
    private const val OP_CODE_RESPONSE_CODE = 6
    private const val OPERATOR_NULL = 0

    fun parse(bytes: DataByteArray): RecordAccessControlPointData? {

        if (bytes.size < 3) {
            return null
        }

        val opCode: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 0) ?: return null
        if (opCode != OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE && opCode != OP_CODE_RESPONSE_CODE) {
            return null
        }

        val operator: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 1) ?: return null
        if (operator != OPERATOR_NULL) {
            return null
        }

        when (opCode) {
            OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE -> {
                // Field size is defined per service
                val numberOfRecords: Int = when (bytes.size - 2) {
                    1 -> bytes.getIntValue(IntFormat.FORMAT_UINT8, 2) ?: return null
                    2 -> bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 2) ?: return null
                    4 -> bytes.getIntValue(IntFormat.FORMAT_UINT32_LE, 2) ?: return null
                    else -> {
                        // Other field sizes are not supported
                        return null
                    }
                }
                return NumberOfRecordsData(numberOfRecords)
            }
            OP_CODE_RESPONSE_CODE -> {
                if (bytes.size != 4) {
                    return null
                }
                val requestCode: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 2) ?: return null
                val racpOpCode = RACPOpCode.create(requestCode)
                val responseCode: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 3) ?: return null
                val racpResponseCode = RACPResponseCode.create(responseCode)
                return ResponseData(racpOpCode, racpResponseCode)
            }
        }
        return null
    }
}