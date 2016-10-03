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

public class TemperatureTypeParser {

	public static String parse(final BluetoothGattCharacteristic characteristic) {
		return parse(characteristic, 0);
	}

	/* package */static String parse(final BluetoothGattCharacteristic characteristic, final int offset) {
		final int type = characteristic.getValue()[offset];

		switch (type) {
		case 1:
			return "Armpit";
		case 2:
			return "Body (general)";
		case 3:
			return "Ear (usually ear lobe)";
		case 4:
			return "Finger";
		case 5:
			return "Gastro-intestinal Tract";
		case 6:
			return "Mouth";
		case 7:
			return "Rectum";
		case 8:
			return "Toe";
		case 9:
			return "Tympanum (ear drum)";
		default:
			return "Unknown";
		}
	}
}
