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
public class GlucoseMeasurementParser {
	private static final int UNIT_kgpl = 0;
	private static final int UNIT_molpl = 1;

	private static final int STATUS_DEVICE_BATTERY_LOW = 0x0001;
	private static final int STATUS_SENSOR_MALFUNCTION = 0x0002;
	private static final int STATUS_SAMPLE_SIZE_FOR_BLOOD_OR_CONTROL_SOLUTION_INSUFFICIENT = 0x0004;
	private static final int STATUS_STRIP_INSERTION_ERROR = 0x0008;
	private static final int STATUS_STRIP_TYPE_INCORRECT_FOR_DEVICE = 0x0010;
	private static final int STATUS_SENSOR_RESULT_TOO_HIGH = 0x0020;
	private static final int STATUS_SENSOR_RESULT_TOO_LOW = 0x0040;
	private static final int STATUS_SENSOR_TEMPERATURE_TOO_HIGH = 0x0080;
	private static final int STATUS_SENSOR_TEMPERATURE_TOO_LOW = 0x0100;
	private static final int STATUS_SENSOR_READ_INTERRUPTED = 0x0200;
	private static final int STATUS_GENERAL_DEVICE_FAULT = 0x0400;
	private static final int STATUS_TIME_FAULT = 0x0800;

	public static String parse(final Data data) {
		final StringBuilder builder = new StringBuilder();

		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset);
		offset += 1;

		final boolean timeOffsetPresent = (flags & 0x01) > 0;
		final boolean typeAndLocationPresent = (flags & 0x02) > 0;
		final int concentrationUnit = (flags & 0x04) > 0 ? UNIT_molpl : UNIT_kgpl;
		final boolean sensorStatusAnnunciationPresent = (flags & 0x08) > 0;
		final boolean contextInfoFollows = (flags & 0x10) > 0;

		// create and fill the new record
		final int sequenceNumber = data.getIntValue(Data.FORMAT_UINT16, offset);
		builder.append("Sequence Number: ").append(sequenceNumber);
		offset += 2;

		builder.append("\nBase Time: ").append(DateTimeParser.parse(data, offset));
		offset += 7;

		if (timeOffsetPresent) {
			// time offset is ignored in the current release
			final int timeOffset = data.getIntValue(Data.FORMAT_SINT16, offset);
			builder.append("\nTime Offset: ").append(timeOffset).append(" min");
			offset += 2;
		}

		if (typeAndLocationPresent) {
			final float glucoseConcentration = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
			final int typeAndLocation = data.getIntValue(Data.FORMAT_UINT8, offset + 2);
			final int type = (typeAndLocation & 0xF0) >> 4; // TODO this way or around?
			final int sampleLocation = (typeAndLocation & 0x0F);
			builder.append("\nGlucose Concentration: ").append(glucoseConcentration).append(concentrationUnit == UNIT_kgpl ? " kg/l" : " mol/l");
			builder.append("\nSample Type: ").append(getType(type));
			builder.append("\nSample Location: ").append(getLocation(sampleLocation));
			offset += 3;
		}

		if (sensorStatusAnnunciationPresent) {
			final int status = data.getIntValue(Data.FORMAT_UINT16, offset);
			builder.append("Status:\n").append(getStatusAnnunciation(status));
		}

		builder.append("\nContext information follows: ").append(contextInfoFollows);
		return builder.toString();
	}

	private static String getType(final int type) {
		switch (type) {
			case 1:
				return "Capillary Whole blood";
			case 2:
				return "Capillary Plasma";
			case 3:
				return "Venous Whole blood";
			case 4:
				return "Venous Plasma";
			case 5:
				return "Arterial Whole blood";
			case 6:
				return "Arterial Plasma";
			case 7:
				return "Undetermined Whole blood";
			case 8:
				return "Undetermined Plasma";
			case 9:
				return "Interstitial Fluid (ISF)";
			case 10:
				return "Control Solution";
			default:
				return "Reserved for future use (" + type + ")";
		}
	}

	private static String getLocation(final int location) {
		switch (location) {
			case 1:
				return "Finger";
			case 2:
				return "Alternate Site Test (AST)";
			case 3:
				return "Earlobe";
			case 4:
				return "Control solution";
			case 15:
				return "Value not available";
			default:
				return "Reserved for future use (" + location + ")";
		}
	}

	private static String getStatusAnnunciation(final int status) {
		final StringBuilder builder = new StringBuilder();
		if ((status & STATUS_DEVICE_BATTERY_LOW) > 0)
			builder.append("\nDevice battery low at time of measurement");
		if ((status & STATUS_SENSOR_MALFUNCTION) > 0)
			builder.append("\nSensor malfunction or faulting at time of measurement");
		if ((status & STATUS_SAMPLE_SIZE_FOR_BLOOD_OR_CONTROL_SOLUTION_INSUFFICIENT) > 0)
			builder.append("\nSample size for blood or control solution insufficient at time of measurement");
		if ((status & STATUS_STRIP_INSERTION_ERROR) > 0)
			builder.append("\nStrip insertion error");
		if ((status & STATUS_STRIP_TYPE_INCORRECT_FOR_DEVICE) > 0)
			builder.append("\nStrip type incorrect for device");
		if ((status & STATUS_SENSOR_RESULT_TOO_HIGH) > 0)
			builder.append("\nSensor result higher than the device can process");
		if ((status & STATUS_SENSOR_RESULT_TOO_LOW) > 0)
			builder.append("\nSensor result lower than the device can process");
		if ((status & STATUS_SENSOR_TEMPERATURE_TOO_HIGH) > 0)
			builder.append("\nSensor temperature too high for valid test/result at time of measurement");
		if ((status & STATUS_SENSOR_TEMPERATURE_TOO_LOW) > 0)
			builder.append("\nSensor temperature too low for valid test/result at time of measurement");
		if ((status & STATUS_SENSOR_READ_INTERRUPTED) > 0)
			builder.append("\nSensor read interrupted because strip was pulled too soon at time of measurement");
		if ((status & STATUS_GENERAL_DEVICE_FAULT) > 0)
			builder.append("\nGeneral device fault has occurred in the sensor");
		if ((status & STATUS_TIME_FAULT) > 0)
			builder.append("\nTime fault has occurred in the sensor and time may be inaccurate");
		return builder.toString();
	}
}
