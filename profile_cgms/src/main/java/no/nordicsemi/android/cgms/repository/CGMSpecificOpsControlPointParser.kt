/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.cgms.repository

import no.nordicsemi.android.ble.data.Data

object CGMSpecificOpsControlPointParser {

    private const val OP_SET_CGM_COMMUNICATION_INTERVAL = 1
    private const val OP_GET_CGM_COMMUNICATION_INTERVAL = 2
    private const val OP_CGM_COMMUNICATION_INTERVAL_RESPONSE = 3
    private const val OP_SET_GLUCOSE_CALIBRATION_VALUE = 4
    private const val OP_GET_GLUCOSE_CALIBRATION_VALUE = 5
    private const val OP_GLUCOSE_CALIBRATION_VALUE_RESPONSE = 6
    private const val OP_SET_PATIENT_HIGH_ALERT_LEVEL = 7
    private const val OP_GET_PATIENT_HIGH_ALERT_LEVEL = 8
    private const val OP_PATIENT_HIGH_ALERT_LEVEL_RESPONSE = 9
    private const val OP_SET_PATIENT_LOW_ALERT_LEVEL = 10
    private const val OP_GET_PATIENT_LOW_ALERT_LEVEL = 11
    private const val OP_PATIENT_LOW_ALERT_LEVEL_RESPONSE = 12
    private const val OP_SET_HYPO_ALERT_LEVEL = 13
    private const val OP_GET_HYPO_ALERT_LEVEL = 14
    private const val OP_HYPO_ALERT_LEVEL_RESPONSE = 15
    private const val OP_SET_HYPER_ALERT_LEVEL = 16
    private const val OP_GET_HYPER_ALERT_LEVEL = 17
    private const val OP_HYPER_ALERT_LEVEL_RESPONSE = 18
    private const val OP_SET_RATE_OF_DECREASE_ALERT_LEVEL = 19
    private const val OP_GET_RATE_OF_DECREASE_ALERT_LEVEL = 20
    private const val OP_RATE_OF_DECREASE_ALERT_LEVEL_RESPONSE = 21
    private const val OP_SET_RATE_OF_INCREASE_ALERT_LEVEL = 22
    private const val OP_GET_RATE_OF_INCREASE_ALERT_LEVEL = 23
    private const val OP_RATE_OF_INCREASE_ALERT_LEVEL_RESPONSE = 24
    private const val OP_RESET_DEVICE_SPECIFIC_ALERT = 25
    private const val OP_CODE_START_SESSION = 26
    private const val OP_CODE_STOP_SESSION = 27
    private const val OP_CODE_RESPONSE_CODE = 28

    // TODO this parser does not support E2E-CRC!
    fun parse(data: Data): String {
        var offset = 0
        val opCode = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
        val builder = StringBuilder()
        builder.append(parseOpCode(opCode))
        when (opCode) {
            OP_SET_CGM_COMMUNICATION_INTERVAL, OP_CGM_COMMUNICATION_INTERVAL_RESPONSE -> {
                val interval = data.getIntValue(Data.FORMAT_UINT8, offset)!!
                builder.append(" to ").append(interval).append(" min")
            }
            OP_SET_GLUCOSE_CALIBRATION_VALUE -> {
                val calConcentration = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
                offset += 2
                val calTime = data.getIntValue(Data.FORMAT_UINT16, offset)!!
                offset += 2
                val calTypeSampleLocation = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
                val calType = calTypeSampleLocation and 0x0F
                val calSampleLocation = calTypeSampleLocation and 0xF0 shr 4
                val calNextCalibrationTime = data.getIntValue(Data.FORMAT_UINT16, offset)!!
                // offset += 2;
                // final int calCalibrationDataRecordNumber = data.getIntValue(Data.FORMAT_UINT16, offset);
                // offset += 2;
                // final int calStatus = data.getIntValue(Data.FORMAT_UINT8, offset++);
                builder.append(" to:\n")
                builder.append("Glucose Concentration of Calibration: ").append(calConcentration)
                    .append(" mg/dL\n")
                builder.append("Time: ").append(calTime).append(" min\n")
                builder.append("Type: ").append(parseType(calType)).append("\n")
                builder.append("Sample Location: ").append(parseSampleLocation(calSampleLocation))
                    .append("\n")
                builder.append("Next Calibration Time: ")
                    .append(parseNextCalibrationTime(calNextCalibrationTime))
                    .append(" min\n") // field ignored on Set
            }
            OP_GET_GLUCOSE_CALIBRATION_VALUE -> {
                val calibrationRecordNumber = data.getIntValue(Data.FORMAT_UINT16, offset)!!
                builder.append(": ").append(parseRecordNumber(calibrationRecordNumber))
            }
            OP_GLUCOSE_CALIBRATION_VALUE_RESPONSE -> {
                val calConcentration = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
                offset += 2
                val calTime = data.getIntValue(Data.FORMAT_UINT16, offset)!!
                offset += 2
                val calTypeSampleLocation = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
                val calType = calTypeSampleLocation and 0x0F
                val calSampleLocation = calTypeSampleLocation and 0xF0 shr 4
                val calNextCalibrationTime = data.getIntValue(Data.FORMAT_UINT16, offset)!!
                offset += 2
                val calCalibrationDataRecordNumber = data.getIntValue(Data.FORMAT_UINT16, offset)!!
                offset += 2
                val calStatus = data.getIntValue(Data.FORMAT_UINT8, offset)!!
                builder.append(":\n")
                if (calCalibrationDataRecordNumber > 0) {
                    builder.append("Glucose Concentration of Calibration: ")
                        .append(calConcentration).append(" mg/dL\n")
                    builder.append("Time: ").append(calTime).append(" min\n")
                    builder.append("Type: ").append(parseType(calType)).append("\n")
                    builder.append("Sample Location: ")
                        .append(parseSampleLocation(calSampleLocation)).append("\n")
                    builder.append("Next Calibration Time: ")
                        .append(parseNextCalibrationTime(calNextCalibrationTime)).append("\n")
                    builder.append("Data Record Number: ").append(calCalibrationDataRecordNumber)
                    parseStatus(builder, calStatus)
                } else {
                    builder.append("No Calibration Data Stored")
                }
            }
            OP_SET_PATIENT_HIGH_ALERT_LEVEL, OP_SET_PATIENT_LOW_ALERT_LEVEL, OP_SET_HYPO_ALERT_LEVEL, OP_SET_HYPER_ALERT_LEVEL -> {
                val level = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
                builder.append(" to: ").append(level).append(" mg/dL")
            }
            OP_PATIENT_HIGH_ALERT_LEVEL_RESPONSE, OP_PATIENT_LOW_ALERT_LEVEL_RESPONSE, OP_HYPO_ALERT_LEVEL_RESPONSE, OP_HYPER_ALERT_LEVEL_RESPONSE -> {
                val level = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
                builder.append(": ").append(level).append(" mg/dL")
            }
            OP_SET_RATE_OF_DECREASE_ALERT_LEVEL, OP_SET_RATE_OF_INCREASE_ALERT_LEVEL -> {
                val level = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
                builder.append(" to: ").append(level).append(" mg/dL/min")
            }
            OP_RATE_OF_DECREASE_ALERT_LEVEL_RESPONSE, OP_RATE_OF_INCREASE_ALERT_LEVEL_RESPONSE -> {
                val level = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
                builder.append(": ").append(level).append(" mg/dL/min")
            }
            OP_CODE_RESPONSE_CODE -> {
                val requestOpCode = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
                val responseCode = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
                builder.append(" to ").append(parseOpCode(requestOpCode)).append(": ").append(
                    parseResponseCode(responseCode)
                )
            }
        }
        return builder.toString()
    }

    private fun parseOpCode(code: Int): String {
        return when (code) {
            OP_SET_CGM_COMMUNICATION_INTERVAL -> "Set CGM Communication Interval"
            OP_GET_CGM_COMMUNICATION_INTERVAL -> "Get CGM Communication Interval"
            OP_CGM_COMMUNICATION_INTERVAL_RESPONSE -> "CGM Communication Interval"
            OP_SET_GLUCOSE_CALIBRATION_VALUE -> "Set CGM Calibration Value"
            OP_GET_GLUCOSE_CALIBRATION_VALUE -> "Get CGM Calibration Value"
            OP_GLUCOSE_CALIBRATION_VALUE_RESPONSE -> "CGM Calibration Value"
            OP_SET_PATIENT_HIGH_ALERT_LEVEL -> "Set Patient High Alert Level"
            OP_GET_PATIENT_HIGH_ALERT_LEVEL -> "Get Patient High Alert Level"
            OP_PATIENT_HIGH_ALERT_LEVEL_RESPONSE -> "Patient High Alert Level"
            OP_SET_PATIENT_LOW_ALERT_LEVEL -> "Set Patient Low Alert Level"
            OP_GET_PATIENT_LOW_ALERT_LEVEL -> "Get Patient Low Alert Level"
            OP_PATIENT_LOW_ALERT_LEVEL_RESPONSE -> "Patient Low Alert Level"
            OP_SET_HYPO_ALERT_LEVEL -> "Set Hypo Alert Level"
            OP_GET_HYPO_ALERT_LEVEL -> "Get Hypo Alert Level"
            OP_HYPO_ALERT_LEVEL_RESPONSE -> "Hypo Alert Level"
            OP_SET_HYPER_ALERT_LEVEL -> "Set Hyper Alert Level"
            OP_GET_HYPER_ALERT_LEVEL -> "Get Hyper Alert Level"
            OP_HYPER_ALERT_LEVEL_RESPONSE -> "Hyper Alert Level"
            OP_SET_RATE_OF_DECREASE_ALERT_LEVEL -> "Set Rate of Decrease Alert Level"
            OP_GET_RATE_OF_DECREASE_ALERT_LEVEL -> "Get Rate of Decrease Alert Level"
            OP_RATE_OF_DECREASE_ALERT_LEVEL_RESPONSE -> "Rate of Decrease Alert Level"
            OP_SET_RATE_OF_INCREASE_ALERT_LEVEL -> "Set Rate of Increase Alert Level"
            OP_GET_RATE_OF_INCREASE_ALERT_LEVEL -> "Get Rate of Increase Alert Level"
            OP_RATE_OF_INCREASE_ALERT_LEVEL_RESPONSE -> "Rate of Increase Alert Level"
            OP_RESET_DEVICE_SPECIFIC_ALERT -> "Reset Device Specific Alert"
            OP_CODE_START_SESSION -> "Start Session"
            OP_CODE_STOP_SESSION -> "Stop Session"
            OP_CODE_RESPONSE_CODE -> "Response"
            else -> "Reserved for future use ($code)"
        }
    }

    private fun parseResponseCode(code: Int): String {
        return when (code) {
            1 -> "Success"
            2 -> "Op Code not supported"
            3 -> "Invalid Operand"
            4 -> "Procedure not completed"
            5 -> "Parameter out of range"
            else -> "Reserved for future use ($code)"
        }
    }

    private fun parseType(type: Int): String {
        return when (type) {
            1 -> "Capillary Whole blood"
            2 -> "Capillary Plasma"
            3 -> "Capillary Whole blood"
            4 -> "Venous Plasma"
            5 -> "Arterial Whole blood"
            6 -> "Arterial Plasma"
            7 -> "Undetermined Whole blood"
            8 -> "Undetermined Plasma"
            9 -> "Interstitial Fluid (ISF)"
            10 -> "Control Solution"
            else -> "Reserved for future use ($type)"
        }
    }

    private fun parseSampleLocation(location: Int): String {
        return when (location) {
            1 -> "Finger"
            2 -> "Alternate Site Test (AST)"
            3 -> "Earlobe"
            4 -> "Control solution"
            5 -> "Subcutaneous tissue"
            15 -> "Sample Location value not available"
            else -> "Reserved for future use ($location)"
        }
    }

    private fun parseNextCalibrationTime(time: Int): String {
        return if (time == 0) "Calibration Required Instantly" else "$time min"
    }

    private fun parseRecordNumber(time: Int): String {
        return if (time == 0xFFFF) "Last Calibration Data" else time.toString()
    }

    private fun parseStatus(builder: StringBuilder, status: Int) {
        if (status == 0) return
        builder.append("\nStatus:\n")
        if (status and 1 > 0) builder.append("- Calibration Data rejected")
        if (status and 2 > 0) builder.append("- Calibration Data out of range")
        if (status and 4 > 0) builder.append("- Calibration Process pending")
        if (status and 0xF8 > 0) builder.append("- Reserved for future use (").append(status)
            .append(")")
    }
}