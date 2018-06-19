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

import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public class CGMSpecificOpsControlPointParser {
	private final static int OP_SET_CGM_COMMUNICATION_INTERVAL = 1;
	private final static int OP_GET_CGM_COMMUNICATION_INTERVAL = 2;
	private final static int OP_CGM_COMMUNICATION_INTERVAL_RESPONSE = 3;
	private final static int OP_SET_GLUCOSE_CALIBRATION_VALUE = 4;
	private final static int OP_GET_GLUCOSE_CALIBRATION_VALUE = 5;
	private final static int OP_GLUCOSE_CALIBRATION_VALUE_RESPONSE = 6;
	private final static int OP_SET_PATIENT_HIGH_ALERT_LEVEL = 7;
	private final static int OP_GET_PATIENT_HIGH_ALERT_LEVEL = 8;
	private final static int OP_PATIENT_HIGH_ALERT_LEVEL_RESPONSE = 9;
	private final static int OP_SET_PATIENT_LOW_ALERT_LEVEL = 10;
	private final static int OP_GET_PATIENT_LOW_ALERT_LEVEL = 11;
	private final static int OP_PATIENT_LOW_ALERT_LEVEL_RESPONSE = 12;
	private final static int OP_SET_HYPO_ALERT_LEVEL = 13;
	private final static int OP_GET_HYPO_ALERT_LEVEL = 14;
	private final static int OP_HYPO_ALERT_LEVEL_RESPONSE = 15;
	private final static int OP_SET_HYPER_ALERT_LEVEL = 16;
	private final static int OP_GET_HYPER_ALERT_LEVEL = 17;
	private final static int OP_HYPER_ALERT_LEVEL_RESPONSE = 18;
	private final static int OP_SET_RATE_OF_DECREASE_ALERT_LEVEL = 19;
	private final static int OP_GET_RATE_OF_DECREASE_ALERT_LEVEL = 20;
	private final static int OP_RATE_OF_DECREASE_ALERT_LEVEL_RESPONSE = 21;
	private final static int OP_SET_RATE_OF_INCREASE_ALERT_LEVEL = 22;
	private final static int OP_GET_RATE_OF_INCREASE_ALERT_LEVEL = 23;
	private final static int OP_RATE_OF_INCREASE_ALERT_LEVEL_RESPONSE = 24;
	private final static int OP_RESET_DEVICE_SPECIFIC_ALERT = 25;
	private final static int OP_CODE_START_SESSION = 26;
	private final static int OP_CODE_STOP_SESSION = 27;
	private final static int OP_CODE_RESPONSE_CODE = 28;

	// TODO this parser does not support E2E-CRC!

	public static String parse(final Data data) {
		int offset = 0;
		final int opCode = data.getIntValue(Data.FORMAT_UINT8, offset++);

		final StringBuilder builder = new StringBuilder();
		builder.append(parseOpCode(opCode));
		switch (opCode) {
			case OP_SET_CGM_COMMUNICATION_INTERVAL:
			case OP_CGM_COMMUNICATION_INTERVAL_RESPONSE: {
				final int interval = data.getIntValue(Data.FORMAT_UINT8, offset);
				builder.append(" to ").append(interval).append(" min");
				break;
			}
			case OP_SET_GLUCOSE_CALIBRATION_VALUE: {
				final float calConcentration = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
				offset += 2;
				final int calTime = data.getIntValue(Data.FORMAT_UINT16, offset);
				offset += 2;
				final int calTypeSampleLocation = data.getIntValue(Data.FORMAT_UINT8, offset++);
				final int calType = calTypeSampleLocation & 0x0F;
				final int calSampleLocation = (calTypeSampleLocation & 0xF0) >> 4;
				final int calNextCalibrationTime = data.getIntValue(Data.FORMAT_UINT16, offset);
				// offset += 2;
				// final int calCalibrationDataRecordNumber = data.getIntValue(Data.FORMAT_UINT16, offset);
				// offset += 2;
				// final int calStatus = data.getIntValue(Data.FORMAT_UINT8, offset++);

				builder.append(" to:\n");
				builder.append("Glucose Concentration of Calibration: ").append(calConcentration).append(" mg/dL\n");
				builder.append("Time: ").append(calTime).append(" min\n");
				builder.append("Type: ").append(parseType(calType)).append("\n");
				builder.append("Sample Location: ").append(parseSampleLocation(calSampleLocation)).append("\n");
				builder.append("Next Calibration Time: ").append(parseNextCalibrationTime(calNextCalibrationTime)).append(" min\n"); // field ignored on Set
				// builder.append("Data Record Number: ").append(calCalibrationDataRecordNumber).append("\n"); // field ignored on Set
				// parseStatus(builder, calStatus); // field ignored on Set
				break;
			}
			case OP_GET_GLUCOSE_CALIBRATION_VALUE: {
				final int calibrationRecordNumber = data.getIntValue(Data.FORMAT_UINT16, offset);
				builder.append(": ").append(parseRecordNumber(calibrationRecordNumber));
				break;
			}
			case OP_GLUCOSE_CALIBRATION_VALUE_RESPONSE: {
				final float calConcentration = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
				offset += 2;
				final int calTime = data.getIntValue(Data.FORMAT_UINT16, offset);
				offset += 2;
				final int calTypeSampleLocation = data.getIntValue(Data.FORMAT_UINT8, offset++);
				final int calType = calTypeSampleLocation & 0x0F;
				final int calSampleLocation = (calTypeSampleLocation & 0xF0) >> 4;
				final int calNextCalibrationTime = data.getIntValue(Data.FORMAT_UINT16, offset);
				offset += 2;
				final int calCalibrationDataRecordNumber = data.getIntValue(Data.FORMAT_UINT16, offset);
				offset += 2;
				final int calStatus = data.getIntValue(Data.FORMAT_UINT8, offset);

				builder.append(":\n");
				if (calCalibrationDataRecordNumber > 0) {
					builder.append("Glucose Concentration of Calibration: ").append(calConcentration).append(" mg/dL\n");
					builder.append("Time: ").append(calTime).append(" min\n");
					builder.append("Type: ").append(parseType(calType)).append("\n");
					builder.append("Sample Location: ").append(parseSampleLocation(calSampleLocation)).append("\n");
					builder.append("Next Calibration Time: ").append(parseNextCalibrationTime(calNextCalibrationTime)).append("\n");
					builder.append("Data Record Number: ").append(calCalibrationDataRecordNumber);
					parseStatus(builder, calStatus);
				} else {
					builder.append("No Calibration Data Stored");
				}
				break;
			}
			case OP_SET_PATIENT_HIGH_ALERT_LEVEL:
			case OP_SET_PATIENT_LOW_ALERT_LEVEL:
			case OP_SET_HYPO_ALERT_LEVEL:
			case OP_SET_HYPER_ALERT_LEVEL: {
				final float level = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
				builder.append(" to: ").append(level).append(" mg/dL");
				break;
			}
			case OP_PATIENT_HIGH_ALERT_LEVEL_RESPONSE:
			case OP_PATIENT_LOW_ALERT_LEVEL_RESPONSE:
			case OP_HYPO_ALERT_LEVEL_RESPONSE:
			case OP_HYPER_ALERT_LEVEL_RESPONSE: {
				final float level = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
				builder.append(": ").append(level).append(" mg/dL");
				break;
			}
			case OP_SET_RATE_OF_DECREASE_ALERT_LEVEL:
			case OP_SET_RATE_OF_INCREASE_ALERT_LEVEL: {
				final float level = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
				builder.append(" to: ").append(level).append(" mg/dL/min");
				break;
			}
			case OP_RATE_OF_DECREASE_ALERT_LEVEL_RESPONSE:
			case OP_RATE_OF_INCREASE_ALERT_LEVEL_RESPONSE: {
				final float level = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
				builder.append(": ").append(level).append(" mg/dL/min");
				break;
			}
			case OP_CODE_RESPONSE_CODE:
				final int requestOpCode = data.getIntValue(Data.FORMAT_UINT8, offset++);
				final int responseCode = data.getIntValue(Data.FORMAT_UINT8, offset++);
				builder.append(" to ").append(parseOpCode(requestOpCode)).append(": ").append(parseResponseCode(responseCode));
				break;
		}

		return builder.toString();
	}

	private static String parseOpCode(final int code) {
		switch (code) {
			case OP_SET_CGM_COMMUNICATION_INTERVAL:
				return "Set CGM Communication Interval";
			case OP_GET_CGM_COMMUNICATION_INTERVAL:
				return "Get CGM Communication Interval";
			case OP_CGM_COMMUNICATION_INTERVAL_RESPONSE:
				return "CGM Communication Interval";
			case OP_SET_GLUCOSE_CALIBRATION_VALUE:
				return "Set CGM Calibration Value";
			case OP_GET_GLUCOSE_CALIBRATION_VALUE:
				return "Get CGM Calibration Value";
			case OP_GLUCOSE_CALIBRATION_VALUE_RESPONSE:
				return "CGM Calibration Value";
			case OP_SET_PATIENT_HIGH_ALERT_LEVEL:
				return "Set Patient High Alert Level";
			case OP_GET_PATIENT_HIGH_ALERT_LEVEL:
				return "Get Patient High Alert Level";
			case OP_PATIENT_HIGH_ALERT_LEVEL_RESPONSE:
				return "Patient High Alert Level";
			case OP_SET_PATIENT_LOW_ALERT_LEVEL:
				return "Set Patient Low Alert Level";
			case OP_GET_PATIENT_LOW_ALERT_LEVEL:
				return "Get Patient Low Alert Level";
			case OP_PATIENT_LOW_ALERT_LEVEL_RESPONSE:
				return "Patient Low Alert Level";
			case OP_SET_HYPO_ALERT_LEVEL:
				return "Set Hypo Alert Level";
			case OP_GET_HYPO_ALERT_LEVEL:
				return "Get Hypo Alert Level";
			case OP_HYPO_ALERT_LEVEL_RESPONSE:
				return "Hypo Alert Level";
			case OP_SET_HYPER_ALERT_LEVEL:
				return "Set Hyper Alert Level";
			case OP_GET_HYPER_ALERT_LEVEL:
				return "Get Hyper Alert Level";
			case OP_HYPER_ALERT_LEVEL_RESPONSE:
				return "Hyper Alert Level";
			case OP_SET_RATE_OF_DECREASE_ALERT_LEVEL:
				return "Set Rate of Decrease Alert Level";
			case OP_GET_RATE_OF_DECREASE_ALERT_LEVEL:
				return "Get Rate of Decrease Alert Level";
			case OP_RATE_OF_DECREASE_ALERT_LEVEL_RESPONSE:
				return "Rate of Decrease Alert Level";
			case OP_SET_RATE_OF_INCREASE_ALERT_LEVEL:
				return "Set Rate of Increase Alert Level";
			case OP_GET_RATE_OF_INCREASE_ALERT_LEVEL:
				return "Get Rate of Increase Alert Level";
			case OP_RATE_OF_INCREASE_ALERT_LEVEL_RESPONSE:
				return "Rate of Increase Alert Level";
			case OP_RESET_DEVICE_SPECIFIC_ALERT:
				return "Reset Device Specific Alert";
			case OP_CODE_START_SESSION:
				return "Start Session";
			case OP_CODE_STOP_SESSION:
				return "Stop Session";
			case OP_CODE_RESPONSE_CODE:
				return "Response";
			default:
				return "Reserved for future use (" + code + ")";
		}
	}

	private static String parseResponseCode(final int code) {
		switch (code) {
			case 1: return "Success";
			case 2: return "Op Code not supported";
			case 3: return "Invalid Operand";
			case 4: return "Procedure not completed";
			case 5: return "Parameter out of range";
			default:
				return "Reserved for future use (" + code + ")";
		}
	}

	private static String parseType(final int type) {
		switch (type) {
			case 1: return "Capillary Whole blood";
			case 2: return "Capillary Plasma";
			case 3: return "Capillary Whole blood";
			case 4: return "Venous Plasma";
			case 5: return "Arterial Whole blood";
			case 6: return "Arterial Plasma";
			case 7: return "Undetermined Whole blood";
			case 8: return "Undetermined Plasma";
			case 9: return "Interstitial Fluid (ISF)";
			case 10: return "Control Solution";
			default: return "Reserved for future use (" + type + ")";
		}
	}

	private static String parseSampleLocation(final int location) {
		switch (location) {
			case 1: return "Finger";
			case 2: return "Alternate Site Test (AST)";
			case 3: return "Earlobe";
			case 4: return "Control solution";
			case 5: return "Subcutaneous tissue";
			case 15: return "Sample Location value not available";
			default: return "Reserved for future use (" + location + ")";
		}
	}

	private static String parseNextCalibrationTime(final int time) {
		if (time == 0)
			return "Calibration Required Instantly";
		return time + " min";
	}

	private static String parseRecordNumber(final int time) {
		if (time == 0xFFFF)
			return "Last Calibration Data";
		return String.valueOf(time);
	}

	private static void parseStatus(final StringBuilder builder, final int status) {
		if (status == 0)
			return;
		builder.append("\nStatus:\n");
		if ((status & 1) > 0)
			builder.append("- Calibration Data rejected");
		if ((status & 2) > 0)
			builder.append("- Calibration Data out of range");
		if ((status & 4) > 0)
			builder.append("- Calibration Process pending");
		if ((status & 0xF8) > 0)
			builder.append("- Reserved for future use (").append(status).append(")");
	}
}
