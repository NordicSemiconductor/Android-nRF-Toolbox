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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public class HeartRateMeasurementParser {
	private static final byte HEART_RATE_VALUE_FORMAT = 0x01; // 1 bit
	private static final byte SENSOR_CONTACT_STATUS = 0x06; // 2 bits
	private static final byte ENERGY_EXPANDED_STATUS = 0x08; // 1 bit
	private static final byte RR_INTERVAL = 0x10; // 1 bit

	public static String parse(final Data data) {
		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset++);

		/*
		 * false 	Heart Rate Value Format is set to UINT8. Units: beats per minute (bpm) 
		 * true 	Heart Rate Value Format is set to UINT16. Units: beats per minute (bpm)
		 */
		final boolean value16bit = (flags & HEART_RATE_VALUE_FORMAT) > 0;

		/*
		 * 0 	Sensor Contact feature is not supported in the current connection
		 * 1 	Sensor Contact feature is not supported in the current connection
		 * 2 	Sensor Contact feature is supported, but contact is not detected
		 * 3 	Sensor Contact feature is supported and contact is detected
		 */
		final int sensorContactStatus = (flags & SENSOR_CONTACT_STATUS) >> 1;

		/*
		 * false 	Energy Expended field is not present
		 * true 	Energy Expended field is present. Units: kilo Joules
		 */
		final boolean energyExpandedStatus = (flags & ENERGY_EXPANDED_STATUS) > 0;

		/*
		 * false 	RR-Interval values are not present.
		 * true 	One or more RR-Interval values are present. Units: 1/1024 seconds
		 */
		final boolean rrIntervalStatus = (flags & RR_INTERVAL) > 0;

		// heart rate value is 8 or 16 bit long
		int heartRateValue = data.getIntValue(value16bit ? Data.FORMAT_UINT16 : Data.FORMAT_UINT8, offset++); // bits per minute
		if (value16bit)
			offset++;

		// energy expanded value is present if a flag was set
		int energyExpanded = -1;
		if (energyExpandedStatus)
			energyExpanded = data.getIntValue(Data.FORMAT_UINT16, offset);
		offset += 2;

		// RR-interval is set when a flag is set
		final List<Float> rrIntervals = new ArrayList<>();
		if (rrIntervalStatus) {
			for (int o = offset; o < data.getValue().length; o += 2) {
				final int units = data.getIntValue(Data.FORMAT_UINT16, o);
				rrIntervals.add(units * 1000.0f / 1024.0f); // RR interval is in [1/1024s]
			}
		}

		final StringBuilder builder = new StringBuilder();
		builder.append("Heart Rate Measurement: ").append(heartRateValue).append(" bpm");
		switch (sensorContactStatus) {
		case 0:
		case 1:
			builder.append(",\nSensor Contact Not Supported");
			break;
		case 2:
			builder.append(",\nContact is NOT Detected");
			break;
		case 3:
			builder.append(",\nContact is Detected");
			break;
		}
		if (energyExpandedStatus)
			builder.append(",\nEnergy Expanded: ").append(energyExpanded).append(" kJ");
		if (rrIntervalStatus) {
			builder.append(",\nRR Interval: ");
			for (final Float interval : rrIntervals)
				builder.append(String.format(Locale.US, "%.02f ms, ", interval));
			builder.setLength(builder.length() - 2); // remove the ", " at the end
		}
		return builder.toString();
	}
}
