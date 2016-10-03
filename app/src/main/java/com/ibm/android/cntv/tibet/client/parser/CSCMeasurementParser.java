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
package com.ibm.android.cntv.tibet.client.parser;

import android.bluetooth.BluetoothGattCharacteristic;

public class CSCMeasurementParser {
	private static final byte WHEEL_REV_DATA_PRESENT = 0x01; // 1 bit
	private static final byte CRANK_REV_DATA_PRESENT = 0x02; // 1 bit

	public static String parse(final BluetoothGattCharacteristic characteristic) {
		int offset = 0;
		final int flags = characteristic.getValue()[offset]; // 1 byte
		offset += 1;

		final boolean wheelRevPresent = (flags & WHEEL_REV_DATA_PRESENT) > 0;
		final boolean crankRevPreset = (flags & CRANK_REV_DATA_PRESENT) > 0;

		int wheelRevolutions = 0;
		int lastWheelEventTime = 0;
		if (wheelRevPresent) {
			wheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
			offset += 4;

			lastWheelEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset); // 1/1024 s
			offset += 2;
		}

		int crankRevolutions = 0;
		int lastCrankEventTime = 0;
		if (crankRevPreset) {
			crankRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
			offset += 2;

			lastCrankEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
			//offset += 2;
		}

		final StringBuilder builder = new StringBuilder();
		if (wheelRevPresent) {
			builder.append(String.format("Wheel rev: %d,\n", wheelRevolutions));
			builder.append(String.format("Last wheel event time: %d ms,\n", lastWheelEventTime));
		}
		if (crankRevPreset) {
			builder.append(String.format("Crank rev: %d,\n", crankRevolutions));
			builder.append(String.format("Last crank event time: %d ms,\n", lastCrankEventTime));
		}
		builder.setLength(builder.length() - 2);
		return builder.toString();
	}
}
