package no.nordicsemi.android.lib.profile.cgms

import android.annotation.SuppressLint
import no.nordicsemi.android.lib.profile.cgms.data.CGMCalibrationStatus
import no.nordicsemi.android.lib.profile.cgms.data.CGMErrorCode
import no.nordicsemi.android.lib.profile.cgms.data.CGMOpCode
import no.nordicsemi.android.lib.profile.cgms.data.CGMSpecificOpsControlPointData
import no.nordicsemi.android.lib.profile.common.CRC16
import no.nordicsemi.kotlin.data.FloatFormat
import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getFloat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder

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

    fun parse(
        data: ByteArray,
        byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    ): CGMSpecificOpsControlPointData? {
        if (data.size < 2) return null

        // Read the Op Code
        val opCode: Int = data.getInt(0, IntFormat.UINT8)

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
        if (data.size != 1 + expectedOperandSize && data.size != 1 + expectedOperandSize + 2) {
            return null
        }

        // Verify CRC if present
        val crcPresent = data.size == 1 + expectedOperandSize + 2 // opCode + expected operand + CRC

        if (crcPresent) {
            val expectedCrc: Int = data.getInt(1 + expectedOperandSize, IntFormat.UINT16, byteOrder)
            val actualCrc: Int = CRC16.MCRF4XX(data, 0, 1 + expectedOperandSize)
            if (expectedCrc != actualCrc) {
                return CGMSpecificOpsControlPointData(
                    isOperationCompleted = false,
                    secured = true,
                    crcValid = false
                )
            }
        }

        when (opCode) {
            OP_CODE_COMMUNICATION_INTERVAL_RESPONSE -> {
                val interval: Int = data.getInt(1, IntFormat.UINT8)

                return CGMSpecificOpsControlPointData(
                    isOperationCompleted = true,
                    requestCode = CGMOpCode.CGM_OP_CODE_SET_COMMUNICATION_INTERVAL,
                    glucoseCommunicationInterval = interval,
                    secured = crcPresent,
                    crcValid = crcPresent,
                )
            }

            OP_CODE_CALIBRATION_VALUE_RESPONSE -> {
                val glucoseConcentrationOfCalibration =
                    data.getFloat(1, FloatFormat.IEEE_11073_16_BIT, byteOrder)
                val calibrationTime = data.getInt(3, IntFormat.UINT16, byteOrder)
                val calibrationTypeAndSampleLocation = data.getInt(5, IntFormat.UINT8)

                @SuppressLint("WrongConstant") val calibrationType =
                    calibrationTypeAndSampleLocation and 0x0F
                val calibrationSampleLocation = calibrationTypeAndSampleLocation shr 4
                val nextCalibrationTime: Int = data.getInt(6, IntFormat.UINT16, byteOrder)
                val calibrationDataRecordNumber: Int = data.getInt(8, IntFormat.UINT16, byteOrder)
                val calibrationStatus: Int = data.getInt(10, IntFormat.UINT8)

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
                val requestCode: Int = data.getInt(1, IntFormat.UINT8)
                val responseCode: Int = data.getInt(2, IntFormat.UINT8)

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
        val alertLevel: Float = data.getFloat(1, FloatFormat.IEEE_11073_16_BIT, byteOrder)

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
