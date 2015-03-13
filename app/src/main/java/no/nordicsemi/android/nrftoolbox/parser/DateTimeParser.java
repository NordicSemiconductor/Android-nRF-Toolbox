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

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Calendar;
import java.util.Locale;

public class DateTimeParser {
	/**
	 * Parses the date and time info.
	 * 
	 * @param characteristic
	 * @return time in human readable format
	 */
	public static String parse(final BluetoothGattCharacteristic characteristic) {
		return parse(characteristic, 0);
	}

	/**
	 * Parses the date and time info. This data has 7 bytes
	 * 
	 * @param characteristic
	 * @param offset
	 *            offset to start reading the time
	 * @return time in human readable format
	 */
	/* package */static String parse(final BluetoothGattCharacteristic characteristic, final int offset) {
		final int year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
		final int month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2);
		final int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 3);
		final int hours = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 4);
		final int minutes = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 5);
		final int seconds = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 6);

		final Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day, hours, minutes, seconds);

		return String.format(Locale.US, "%1$te %1$tb %1$tY, %1$tH:%1$tM:%1$tS", calendar);
	}
}
