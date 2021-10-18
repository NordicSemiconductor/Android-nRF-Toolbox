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
import java.util.*

object CGMMeasurementParser {

    private const val FLAGS_CGM_TREND_INFO_PRESENT = 1
    private const val FLAGS_CGM_QUALITY_PRESENT = 1 shl 1
    private const val FLAGS_SENSOR_STATUS_ANNUNCIATION_WARNING_OCTET_PRESENT = 1 shl 2
    private const val FLAGS_SENSOR_STATUS_ANNUNCIATION_CAL_TEMP_OCTET_PRESENT = 1 shl 3
    private const val FLAGS_SENSOR_STATUS_ANNUNCIATION_STATUS_OCTET_PRESENT = 1 shl 4
    private const val SSA_SESSION_STOPPED = 1
    private const val SSA_DEVICE_BATTERY_LOW = 1 shl 1
    private const val SSA_SENSOR_TYPE_INCORRECT = 1 shl 2
    private const val SSA_SENSOR_MALFUNCTION = 1 shl 3
    private const val SSA_DEVICE_SPEC_ALERT = 1 shl 4
    private const val SSA_GENERAL_DEVICE_FAULT = 1 shl 5
    private const val SSA_TIME_SYNC_REQUIRED = 1 shl 8
    private const val SSA_CALIBRATION_NOT_ALLOWED = 1 shl 9
    private const val SSA_CALIBRATION_RECOMMENDED = 1 shl 10
    private const val SSA_CALIBRATION_REQUIRED = 1 shl 11
    private const val SSA_SENSOR_TEMP_TOO_HIGH = 1 shl 12
    private const val SSA_SENSOR_TEMP_TOO_LOW = 1 shl 13
    private const val SSA_RESULT_LOWER_THAN_PATIENT_LOW_LEVEL = 1 shl 16
    private const val SSA_RESULT_HIGHER_THAN_PATIENT_HIGH_LEVEL = 1 shl 17
    private const val SSA_RESULT_LOWER_THAN_HYPO_LEVEL = 1 shl 18
    private const val SSA_RESULT_HIGHER_THAN_HYPER_LEVEL = 1 shl 19
    private const val SSA_SENSOR_RATE_OF_DECREASE_EXCEEDED = 1 shl 20
    private const val SSA_SENSOR_RATE_OF_INCREASE_EXCEEDED = 1 shl 21
    private const val SSA_RESULT_LOWER_THAN_DEVICE_CAN_PROCESS = 1 shl 22
    private const val SSA_RESULT_HIGHER_THAN_DEVICE_CAN_PROCESS = 1 shl 23

    fun parse(data: Data): String {
        // The CGM Measurement characteristic is a variable length structure containing one or more CGM Measurement records
        val totalSize = data.value!!.size
        val builder = StringBuilder()
        var offset = 0
        while (offset < totalSize) {
            offset += parseRecord(builder, data, offset)
            if (offset < totalSize) builder.append("\n\n")
        }
        return builder.toString()
    }

    private fun parseRecord(builder: StringBuilder, data: Data, offset: Int): Int {
        // Read size and flags bytes
        var offset = offset
        val size = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
        val flags = data.getIntValue(Data.FORMAT_UINT8, offset++)!!

        /*
		 * false 	CGM Trend Information is not preset
		 * true 	CGM Trend Information is preset
		 */
        val cgmTrendInformationPresent = flags and FLAGS_CGM_TREND_INFO_PRESENT > 0

        /*
		 * false 	CGM Quality is not preset
		 * true 	CGM Quality is preset
		 */
        val cgmQualityPresent = flags and FLAGS_CGM_QUALITY_PRESENT > 0

        /*
		 * false 	Sensor Status Annunciation - Warning-Octet is not preset
		 * true 	Sensor Status Annunciation - Warning-Octet is preset
		 */
        val ssaWarningOctetPresent =
            flags and FLAGS_SENSOR_STATUS_ANNUNCIATION_WARNING_OCTET_PRESENT > 0

        /*
		 * false 	Sensor Status Annunciation - Calibration/Temp-Octet is not preset
		 * true 	Sensor Status Annunciation - Calibration/Temp-Octet is preset
		 */
        val ssaCalTempOctetPresent =
            flags and FLAGS_SENSOR_STATUS_ANNUNCIATION_CAL_TEMP_OCTET_PRESENT > 0

        /*
		 * false 	Sensor Status Annunciation - Status-Octet is not preset
		 * true 	Sensor Status Annunciation - Status-Octet is preset
		 */
        val ssaStatusOctetPresent =
            flags and FLAGS_SENSOR_STATUS_ANNUNCIATION_STATUS_OCTET_PRESENT > 0

        // Read CGM Glucose Concentration
        val glucoseConcentration = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
        offset += 2

        // Read time offset
        val timeOffset = data.getIntValue(Data.FORMAT_UINT16, offset)!!
        offset += 2
        builder.append("Glucose concentration: ").append(glucoseConcentration).append(" mg/dL\n")
        builder.append("Sequence number: ").append(timeOffset).append(" (Time Offset in min)\n")
        if (ssaWarningOctetPresent) {
            val ssaWarningOctet = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
            builder.append("Warnings:\n")
            if (ssaWarningOctet and SSA_SESSION_STOPPED > 0) builder.append("- Session Stopped\n")
            if (ssaWarningOctet and SSA_DEVICE_BATTERY_LOW > 0) builder.append("- Device Battery Low\n")
            if (ssaWarningOctet and SSA_SENSOR_TYPE_INCORRECT > 0) builder.append("- Sensor Type Incorrect\n")
            if (ssaWarningOctet and SSA_SENSOR_MALFUNCTION > 0) builder.append("- Sensor Malfunction\n")
            if (ssaWarningOctet and SSA_DEVICE_SPEC_ALERT > 0) builder.append("- Device Specific Alert\n")
            if (ssaWarningOctet and SSA_GENERAL_DEVICE_FAULT > 0) builder.append("- General Device Fault\n")
        }
        if (ssaCalTempOctetPresent) {
            val ssaCalTempOctet = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
            builder.append("Cal/Temp Info:\n")
            if (ssaCalTempOctet and SSA_TIME_SYNC_REQUIRED > 0) builder.append("- Time Synchronization Required\n")
            if (ssaCalTempOctet and SSA_CALIBRATION_NOT_ALLOWED > 0) builder.append("- Calibration Not Allowed\n")
            if (ssaCalTempOctet and SSA_CALIBRATION_RECOMMENDED > 0) builder.append("- Calibration Recommended\n")
            if (ssaCalTempOctet and SSA_CALIBRATION_REQUIRED > 0) builder.append("- Calibration Required\n")
            if (ssaCalTempOctet and SSA_SENSOR_TEMP_TOO_HIGH > 0) builder.append("- Sensor Temp Too High\n")
            if (ssaCalTempOctet and SSA_SENSOR_TEMP_TOO_LOW > 0) builder.append("- Sensor Temp Too Low\n")
        }
        if (ssaStatusOctetPresent) {
            val ssaStatusOctet = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
            builder.append("Status:\n")
            if (ssaStatusOctet and SSA_RESULT_LOWER_THAN_PATIENT_LOW_LEVEL > 0) builder.append("- Result Lower then Patient Low Level\n")
            if (ssaStatusOctet and SSA_RESULT_HIGHER_THAN_PATIENT_HIGH_LEVEL > 0) builder.append("- Result Higher then Patient High Level\n")
            if (ssaStatusOctet and SSA_RESULT_LOWER_THAN_HYPO_LEVEL > 0) builder.append("- Result Lower then Hypo Level\n")
            if (ssaStatusOctet and SSA_RESULT_HIGHER_THAN_HYPER_LEVEL > 0) builder.append("- Result Higher then Hyper Level\n")
            if (ssaStatusOctet and SSA_SENSOR_RATE_OF_DECREASE_EXCEEDED > 0) builder.append("- Sensor Rate of Decrease Exceeded\n")
            if (ssaStatusOctet and SSA_SENSOR_RATE_OF_INCREASE_EXCEEDED > 0) builder.append("- Sensor Rate of Increase Exceeded\n")
            if (ssaStatusOctet and SSA_RESULT_LOWER_THAN_DEVICE_CAN_PROCESS > 0) builder.append("- Result Lower then Device Can Process\n")
            if (ssaStatusOctet and SSA_RESULT_HIGHER_THAN_DEVICE_CAN_PROCESS > 0) builder.append("- Result Higher then Device Can Process\n")
        }
        if (cgmTrendInformationPresent) {
            val trend = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
            offset += 2
            builder.append("Trend: ").append(trend).append(" mg/dL/min\n")
        }
        if (cgmQualityPresent) {
            val quality = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
            offset += 2
            builder.append("Quality: ").append(quality).append("%\n")
        }
        if (size > offset + 1) {
            val crc = data.getIntValue(Data.FORMAT_UINT16, offset)!!
            // offset += 2;
            builder.append(String.format(Locale.US, "E2E-CRC: 0x%04X\n", crc))
        }
        builder.setLength(builder.length - 1) // Remove last \n
        return size
    }
}
