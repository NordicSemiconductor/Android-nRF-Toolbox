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
public class IntermediateCuffPressureParser {
	public static String parse(final Data data) {
		final StringBuilder builder = new StringBuilder();

		// first byte - flags
		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset++);

		final int unitType = flags & 0x01;
		final boolean timestampPresent = (flags & 0x02) > 0;
		final boolean pulseRatePresent = (flags & 0x04) > 0;
		final boolean userIdPresent = (flags & 0x08) > 0;
		final boolean statusPresent = (flags & 0x10) > 0;

		// following bytes - pressure
		final float pressure = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
		final String unit = unitType == 0 ? " mmHg" : " kPa";
		offset += 6;
		builder.append("Cuff pressure: ").append(pressure).append(unit);

		// parse timestamp if present
		if (timestampPresent) {
			builder.append("Timestamp: ").append(DateTimeParser.parse(data, offset));
			offset += 7;
		}

		// parse pulse rate if present
		if (pulseRatePresent) {
			final float pulseRate = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
			offset += 2;
			builder.append("\nPulse: ").append(pulseRate).append(" bpm");
		}

		if (userIdPresent) {
			final int userId = data.getIntValue(Data.FORMAT_UINT8, offset);
			offset += 1;
			builder.append("\nUser ID: ").append(userId);
		}

		if (statusPresent) {
			final int status = data.getIntValue(Data.FORMAT_UINT16, offset);
			// offset += 2;
			if ((status & 0x0001) > 0)
				builder.append("\nBody movement detected");
			if ((status & 0x0002) > 0)
				builder.append("\nCuff too lose");
			if ((status & 0x0004) > 0)
				builder.append("\nIrregular pulse detected");
			if ((status & 0x0018) == 0x0008)
				builder.append("\nPulse rate exceeds upper limit");
			if ((status & 0x0018) == 0x0010)
				builder.append("\nPulse rate is less than lower limit");
			if ((status & 0x0018) == 0x0018)
				builder.append("\nPulse rate range: Reserved for future use ");
			if ((status & 0x0020) > 0)
				builder.append("\nImproper measurement position");
		}

		return builder.toString();
	}
}
