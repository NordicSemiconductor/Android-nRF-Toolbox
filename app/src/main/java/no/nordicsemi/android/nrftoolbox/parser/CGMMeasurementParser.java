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
package no.nordicsemi.android.nrftoolbox.parser;

import java.util.Locale;

import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public class CGMMeasurementParser {
	private static final int FLAGS_CGM_TREND_INFO_PRESENT = 1;
	private static final int FLAGS_CGM_QUALITY_PRESENT = 1 << 1;
	private static final int FLAGS_SENSOR_STATUS_ANNUNCIATION_WARNING_OCTET_PRESENT = 1 << 2;
	private static final int FLAGS_SENSOR_STATUS_ANNUNCIATION_CAL_TEMP_OCTET_PRESENT = 1 << 3;
	private static final int FLAGS_SENSOR_STATUS_ANNUNCIATION_STATUS_OCTET_PRESENT = 1 << 4;

	private static final int SSA_SESSION_STOPPED = 1;
	private static final int SSA_DEVICE_BATTERY_LOW = 1 << 1;
	private static final int SSA_SENSOR_TYPE_INCORRECT = 1 << 2;
	private static final int SSA_SENSOR_MALFUNCTION = 1 << 3;
	private static final int SSA_DEVICE_SPEC_ALERT = 1 << 4;
	private static final int SSA_GENERAL_DEVICE_FAULT = 1 << 5;

	private static final int SSA_TIME_SYNC_REQUIRED = 1 << 8;
	private static final int SSA_CALIBRATION_NOT_ALLOWED = 1 << 9;
	private static final int SSA_CALIBRATION_RECOMMENDED = 1 << 10;
	private static final int SSA_CALIBRATION_REQUIRED = 1 << 11;
	private static final int SSA_SENSOR_TEMP_TOO_HIGH = 1 << 12;
	private static final int SSA_SENSOR_TEMP_TOO_LOW = 1 << 13;

	private static final int SSA_RESULT_LOWER_THAN_PATIENT_LOW_LEVEL = 1 << 16;
	private static final int SSA_RESULT_HIGHER_THAN_PATIENT_HIGH_LEVEL = 1 << 17;
	private static final int SSA_RESULT_LOWER_THAN_HYPO_LEVEL = 1 << 18;
	private static final int SSA_RESULT_HIGHER_THAN_HYPER_LEVEL = 1 << 19;
	private static final int SSA_SENSOR_RATE_OF_DECREASE_EXCEEDED = 1 << 20;
	private static final int SSA_SENSOR_RATE_OF_INCREASE_EXCEEDED = 1 << 21;
	private static final int SSA_RESULT_LOWER_THAN_DEVICE_CAN_PROCESS = 1 << 22;
	private static final int SSA_RESULT_HIGHER_THAN_DEVICE_CAN_PROCESS = 1 << 23;

	public static String parse(final Data data) {
		// The CGM Measurement characteristic is a variable length structure containing one or more CGM Measurement records
		int totalSize = data.getValue().length;

		final StringBuilder builder = new StringBuilder();
		int offset = 0;
		while (offset < totalSize) {
			offset += parseRecord(builder, data, offset);
			if (offset <  totalSize)
				builder.append("\n\n");
		}
		return builder.toString();
	}

	private static int parseRecord(final StringBuilder builder, final Data data, int offset) {
		// Read size and flags bytes
		final int size = data.getIntValue(Data.FORMAT_UINT8, offset++);
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset++);

		/*
		 * false 	CGM Trend Information is not preset
		 * true 	CGM Trend Information is preset
		 */
		final boolean cgmTrendInformationPresent = (flags & FLAGS_CGM_TREND_INFO_PRESENT) > 0;

		/*
		 * false 	CGM Quality is not preset
		 * true 	CGM Quality is preset
		 */
		final boolean cgmQualityPresent = (flags & FLAGS_CGM_QUALITY_PRESENT) > 0;

		/*
		 * false 	Sensor Status Annunciation - Warning-Octet is not preset
		 * true 	Sensor Status Annunciation - Warning-Octet is preset
		 */
		final boolean ssaWarningOctetPresent = (flags & FLAGS_SENSOR_STATUS_ANNUNCIATION_WARNING_OCTET_PRESENT) > 0;

		/*
		 * false 	Sensor Status Annunciation - Calibration/Temp-Octet is not preset
		 * true 	Sensor Status Annunciation - Calibration/Temp-Octet is preset
		 */
		final boolean ssaCalTempOctetPresent = (flags & FLAGS_SENSOR_STATUS_ANNUNCIATION_CAL_TEMP_OCTET_PRESENT) > 0;

		/*
		 * false 	Sensor Status Annunciation - Status-Octet is not preset
		 * true 	Sensor Status Annunciation - Status-Octet is preset
		 */
		final boolean ssaStatusOctetPresent = (flags & FLAGS_SENSOR_STATUS_ANNUNCIATION_STATUS_OCTET_PRESENT) > 0;

		// Read CGM Glucose Concentration
		final float glucoseConcentration = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
		offset += 2;

		// Read time offset
		final int timeOffset = data.getIntValue(Data.FORMAT_UINT16, offset);
		offset += 2;

		builder.append("Glucose concentration: ").append(glucoseConcentration).append(" mg/dL\n");
		builder.append("Sequence number: ").append(timeOffset).append(" (Time Offset in min)\n");

		if (ssaWarningOctetPresent) {
			final int ssaWarningOctet = data.getIntValue(Data.FORMAT_UINT8, offset++);
			builder.append("Warnings:\n");
			if ((ssaWarningOctet & SSA_SESSION_STOPPED) > 0)
				builder.append("- Session Stopped\n");
			if ((ssaWarningOctet & SSA_DEVICE_BATTERY_LOW) > 0)
				builder.append("- Device Battery Low\n");
			if ((ssaWarningOctet & SSA_SENSOR_TYPE_INCORRECT) > 0)
				builder.append("- Sensor Type Incorrect\n");
			if ((ssaWarningOctet & SSA_SENSOR_MALFUNCTION) > 0)
				builder.append("- Sensor Malfunction\n");
			if ((ssaWarningOctet & SSA_DEVICE_SPEC_ALERT) > 0)
				builder.append("- Device Specific Alert\n");
			if ((ssaWarningOctet & SSA_GENERAL_DEVICE_FAULT) > 0)
				builder.append("- General Device Fault\n");
		}

		if (ssaCalTempOctetPresent) {
			final int ssaCalTempOctet = data.getIntValue(Data.FORMAT_UINT8, offset++);
			builder.append("Cal/Temp Info:\n");
			if ((ssaCalTempOctet & SSA_TIME_SYNC_REQUIRED) > 0)
				builder.append("- Time Synchronization Required\n");
			if ((ssaCalTempOctet & SSA_CALIBRATION_NOT_ALLOWED) > 0)
				builder.append("- Calibration Not Allowed\n");
			if ((ssaCalTempOctet & SSA_CALIBRATION_RECOMMENDED) > 0)
				builder.append("- Calibration Recommended\n");
			if ((ssaCalTempOctet & SSA_CALIBRATION_REQUIRED) > 0)
				builder.append("- Calibration Required\n");
			if ((ssaCalTempOctet & SSA_SENSOR_TEMP_TOO_HIGH) > 0)
				builder.append("- Sensor Temp Too High\n");
			if ((ssaCalTempOctet & SSA_SENSOR_TEMP_TOO_LOW) > 0)
				builder.append("- Sensor Temp Too Low\n");
		}

		if (ssaStatusOctetPresent) {
			final int ssaStatusOctet = data.getIntValue(Data.FORMAT_UINT8, offset++);
			builder.append("Status:\n");
			if ((ssaStatusOctet & SSA_RESULT_LOWER_THAN_PATIENT_LOW_LEVEL) > 0)
				builder.append("- Result Lower then Patient Low Level\n");
			if ((ssaStatusOctet & SSA_RESULT_HIGHER_THAN_PATIENT_HIGH_LEVEL) > 0)
				builder.append("- Result Higher then Patient High Level\n");
			if ((ssaStatusOctet & SSA_RESULT_LOWER_THAN_HYPO_LEVEL) > 0)
				builder.append("- Result Lower then Hypo Level\n");
			if ((ssaStatusOctet & SSA_RESULT_HIGHER_THAN_HYPER_LEVEL) > 0)
				builder.append("- Result Higher then Hyper Level\n");
			if ((ssaStatusOctet & SSA_SENSOR_RATE_OF_DECREASE_EXCEEDED) > 0)
				builder.append("- Sensor Rate of Decrease Exceeded\n");
			if ((ssaStatusOctet & SSA_SENSOR_RATE_OF_INCREASE_EXCEEDED) > 0)
				builder.append("- Sensor Rate of Increase Exceeded\n");
			if ((ssaStatusOctet & SSA_RESULT_LOWER_THAN_DEVICE_CAN_PROCESS) > 0)
				builder.append("- Result Lower then Device Can Process\n");
			if ((ssaStatusOctet & SSA_RESULT_HIGHER_THAN_DEVICE_CAN_PROCESS) > 0)
				builder.append("- Result Higher then Device Can Process\n");
		}

		if (cgmTrendInformationPresent) {
			final float trend = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
			offset += 2;
			builder.append("Trend: ").append(trend).append(" mg/dL/min\n");
		}

		if (cgmQualityPresent) {
			final float quality = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
			offset += 2;
			builder.append("Quality: ").append(quality).append("%\n");
		}

		if (size > offset + 1) {
			final int crc = data.getIntValue(Data.FORMAT_UINT16, offset);
			// offset += 2;
			builder.append(String.format(Locale.US, "E2E-CRC: 0x%04X\n", crc));
		}
		builder.setLength(builder.length() - 1); // Remove last \n
		return size;
	}
}
