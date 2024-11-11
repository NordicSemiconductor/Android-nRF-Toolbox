package no.nordicsemi.android.toolbox.libs.core.data.cgms

import android.annotation.SuppressLint
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMCalibrationStatus
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMErrorCode
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMOpCode
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMSpecificOpsControlPointData
import no.nordicsemi.android.toolbox.libs.core.data.common.CRC16

object CGMSpecificOpsControlPointParser {

    private const val OP_CODE_COMMUNICATION_INTERVAL_RESPONSE = 3
    private const val OP_CODE_CALIBRATION_VALUE_RESPONSE = 6
    private const val OP_CODE_PATIENT_HIGH_ALERT_LEVEL_RESPONSE = 9
    private const val OP_CODE_PATIENT_LOW_ALERT_LEVEL_RESPONSE = 12
    private const val OP_CODE_HYPO_ALERT_LEVEL_RESPONSE = 15
    private const val OP_CODE_HYPER_ALERT_LEVEL_RESPONSE = 18
    private const val OP_CODE_RATE_OF_DECREASE_ALERT_LEVEL_RESPONSE = 21
    private const val OP_CODE_RATE_OF_INCREASE_ALERT_LEVEL_RESPONSE = 24
    private const val OP_CODE_RESPONSE_CODE = 28
    private const val CGM_RESPONSE_SUCCESS = 1

    fun parse(data: ByteArray): CGMSpecificOpsControlPointData? {
        val bytes = DataByteArray(data)
        if (bytes.size < 2) {
            return null
        }

        // Read the Op Code
        val opCode: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 0) ?: return null

        // Estimate the expected operand size based on the Op Code
        val expectedOperandSize: Int = when (opCode) {
            OP_CODE_COMMUNICATION_INTERVAL_RESPONSE -> 1
            OP_CODE_CALIBRATION_VALUE_RESPONSE -> 10
            OP_CODE_PATIENT_HIGH_ALERT_LEVEL_RESPONSE,
            OP_CODE_PATIENT_LOW_ALERT_LEVEL_RESPONSE,
            OP_CODE_HYPO_ALERT_LEVEL_RESPONSE,
            OP_CODE_HYPER_ALERT_LEVEL_RESPONSE,
            OP_CODE_RATE_OF_DECREASE_ALERT_LEVEL_RESPONSE,
            OP_CODE_RATE_OF_INCREASE_ALERT_LEVEL_RESPONSE -> 2
            OP_CODE_RESPONSE_CODE -> 2
            else -> return null
        }

        // Verify packet length
        if (bytes.size != 1 + expectedOperandSize && bytes.size != 1 + expectedOperandSize + 2) {
            return null
        }

        // Verify CRC if present
        val crcPresent = bytes.size == 1 + expectedOperandSize + 2 // opCode + expected operand + CRC

        if (crcPresent) {
            val expectedCrc: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 1 + expectedOperandSize) ?: return null
            val actualCrc: Int = CRC16.MCRF4XX(bytes.value, 0, 1 + expectedOperandSize)
            if (expectedCrc != actualCrc) {
                return CGMSpecificOpsControlPointData(isOperationCompleted = false, secured = true, crcValid = false)
            }
        }

        when (opCode) {
            OP_CODE_COMMUNICATION_INTERVAL_RESPONSE -> {
                val interval: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 1) ?: return null
                return CGMSpecificOpsControlPointData(
                    isOperationCompleted = true,
                    requestCode = CGMOpCode.CGM_OP_CODE_SET_COMMUNICATION_INTERVAL,
                    glucoseCommunicationInterval = interval,
                    secured = crcPresent,
                    crcValid = crcPresent,
                )
            }
            OP_CODE_CALIBRATION_VALUE_RESPONSE -> {
                val glucoseConcentrationOfCalibration: Float = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, 1) ?: return null
                val calibrationTime: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 3) ?: return null
                val calibrationTypeAndSampleLocation: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 5) ?: return null
                @SuppressLint("WrongConstant") val calibrationType = calibrationTypeAndSampleLocation and 0x0F
                val calibrationSampleLocation = calibrationTypeAndSampleLocation shr 4
                val nextCalibrationTime: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 6) ?: return null
                val calibrationDataRecordNumber: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 8) ?: return null
                val calibrationStatus: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 10) ?: return null
                return CGMSpecificOpsControlPointData(
                    glucoseConcentrationOfCalibration = glucoseConcentrationOfCalibration,
                    calibrationTime = calibrationTime,
                    nextCalibrationTime = nextCalibrationTime,
                    type = calibrationType,
                    sampleLocation = calibrationSampleLocation,
                    calibrationDataRecordNumber = calibrationDataRecordNumber,
                    calibrationStatus = CGMCalibrationStatus(calibrationStatus),
                    crcValid = crcPresent,
                    secured = crcPresent
                )
            }
            OP_CODE_RESPONSE_CODE -> {
                val requestCode: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 1) ?: return null
                val responseCode: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 2) ?: return null
                if (responseCode == CGM_RESPONSE_SUCCESS) {
                    return CGMSpecificOpsControlPointData(
                        isOperationCompleted = true,
                        requestCode = CGMOpCode.create(requestCode),
                        secured = crcPresent,
                        crcValid = crcPresent,
                    )
                } else {
                    return CGMSpecificOpsControlPointData(
                        isOperationCompleted = false,
                        requestCode = CGMOpCode.create(requestCode),
                        errorCode = CGMErrorCode.create(responseCode),
                        secured = crcPresent,
                        crcValid = crcPresent
                    )
                }
            }
        }

        // Read SFLOAT value
        val alertLevel: Float = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, 1) ?: return null
        when (opCode) {
            OP_CODE_PATIENT_HIGH_ALERT_LEVEL_RESPONSE -> {
                return CGMSpecificOpsControlPointData(
                    isOperationCompleted = true,
                    requestCode = CGMOpCode.CGM_OP_CODE_SET_PATIENT_HIGH_ALERT_LEVEL,
                    alertLevel = alertLevel,
                    secured = crcPresent,
                    crcValid = crcPresent
                )
            }
            OP_CODE_PATIENT_LOW_ALERT_LEVEL_RESPONSE -> {
                return CGMSpecificOpsControlPointData(
                    isOperationCompleted = true,
                    requestCode = CGMOpCode.CGM_OP_CODE_SET_PATIENT_LOW_ALERT_LEVEL,
                    alertLevel = alertLevel,
                    secured = crcPresent,
                    crcValid = crcPresent
                )
            }
            OP_CODE_HYPO_ALERT_LEVEL_RESPONSE -> {
                return CGMSpecificOpsControlPointData(
                    isOperationCompleted = true,
                    requestCode = CGMOpCode.CGM_OP_CODE_SET_HYPO_ALERT_LEVEL,
                    alertLevel = alertLevel,
                    secured = crcPresent,
                    crcValid = crcPresent
                )
            }
            OP_CODE_HYPER_ALERT_LEVEL_RESPONSE -> {
                return CGMSpecificOpsControlPointData(
                    isOperationCompleted = true,
                    requestCode = CGMOpCode.CGM_OP_CODE_SET_HYPER_ALERT_LEVEL,
                    alertLevel = alertLevel,
                    secured = crcPresent,
                    crcValid = crcPresent
                )
            }
            OP_CODE_RATE_OF_DECREASE_ALERT_LEVEL_RESPONSE -> {
                return CGMSpecificOpsControlPointData(
                    isOperationCompleted = true,
                    requestCode = CGMOpCode.CGM_OP_CODE_SET_RATE_OF_DECREASE_ALERT_LEVEL,
                    alertLevel = alertLevel,
                    secured = crcPresent,
                    crcValid = crcPresent
                )
            }
            OP_CODE_RATE_OF_INCREASE_ALERT_LEVEL_RESPONSE -> {
                return CGMSpecificOpsControlPointData(
                    isOperationCompleted = true,
                    requestCode = CGMOpCode.CGM_OP_CODE_SET_RATE_OF_INCREASE_ALERT_LEVEL,
                    alertLevel = alertLevel,
                    secured = crcPresent,
                    crcValid = crcPresent
                )
            }
        }
        return null
    }
}
