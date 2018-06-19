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

// TODO this method may be used for developing purposes to log the data from your device using the nRF Logger application.

@SuppressWarnings("ConstantConditions")
public class TemplateParser {
	// TODO add some flags, if needed
	private static final byte HEART_RATE_VALUE_FORMAT = 0x01; // 1 bit

	/**
	 * This method converts the value of the characteristic to the String. The String is then logged in the nRF logger log session
	 * @param data the characteristic data to be parsed
	 * @return human readable value of the characteristic
	 */
	@SuppressWarnings("UnusedAssignment")
	public static String parse(final Data data) {
		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset++);

		/*
		 * In the template we are using the HRM values as an example.
		 * false 	Heart Rate Value Format is set to UINT8. Units: beats per minute (bpm) 
		 * true 	Heart Rate Value Format is set to UINT16. Units: beats per minute (bpm)
		 */
		final boolean value16bit = (flags & HEART_RATE_VALUE_FORMAT) > 0;

		// heart rate value is 8 or 16 bit long
		int value = data.getIntValue(value16bit ? Data.FORMAT_UINT16 : Data.FORMAT_UINT8, offset++); // bits per minute
		if (value16bit)
			offset++;

		// TODO parse more data

		return "Template Measurement: " + value + " bpm";
	}
}
