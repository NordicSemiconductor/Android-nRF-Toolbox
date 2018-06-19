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
public class RSCMeasurementParser {
	private static final byte INSTANTANEOUS_STRIDE_LENGTH_PRESENT = 0x01; // 1 bit
	private static final byte TOTAL_DISTANCE_PRESENT = 0x02; // 1 bit
	private static final byte WALKING_OR_RUNNING_STATUS_BITS = 0x04; // 1 bit

	public static String parse(final Data data) {
		int offset = 0;
		final int flags = data.getValue()[offset]; // 1 byte
		offset += 1;

		final boolean islmPresent = (flags & INSTANTANEOUS_STRIDE_LENGTH_PRESENT) > 0;
		final boolean tdPreset = (flags & TOTAL_DISTANCE_PRESENT) > 0;
		final boolean running = (flags & WALKING_OR_RUNNING_STATUS_BITS) > 0;
		final boolean walking = !running;

		final float instantaneousSpeed = (float) data.getIntValue(Data.FORMAT_UINT16, offset) / 256.0f; // 1/256 m/s
		offset += 2;

		final int instantaneousCadence = data.getIntValue(Data.FORMAT_UINT8, offset);
		offset += 1;

		float instantaneousStrideLength = 0;
		if (islmPresent) {
			instantaneousStrideLength = (float) data.getIntValue(Data.FORMAT_UINT16, offset) / 100.0f; // 1/100 m
			offset += 2;
		}

		float totalDistance = 0;
		if (tdPreset) {
			totalDistance = (float) data.getIntValue(Data.FORMAT_UINT32, offset) / 10.0f;
			// offset += 4;
		}

		final StringBuilder builder = new StringBuilder();
		builder.append(String.format(Locale.US, "Speed: %.2f m/s, Cadence: %d RPM,\n", instantaneousSpeed, instantaneousCadence));
		if (islmPresent)
			builder.append(String.format(Locale.US, "Instantaneous Stride Length: %.2f m,\n", instantaneousStrideLength));
		if (tdPreset)
			builder.append(String.format(Locale.US, "Total Distance: %.1f m,\n", totalDistance));
		if (walking)
			builder.append("Status: WALKING");
		else
			builder.append("Status: RUNNING");
		return builder.toString();
	}
}
