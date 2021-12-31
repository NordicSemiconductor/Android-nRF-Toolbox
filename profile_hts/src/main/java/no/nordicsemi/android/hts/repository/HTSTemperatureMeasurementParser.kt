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
package no.nordicsemi.android.hts.repository

import no.nordicsemi.android.ble.data.Data
import java.util.*

private const val TEMPERATURE_UNIT_FLAG: Byte = 0x01 // 1 bit
private const val TIMESTAMP_FLAG: Byte = 0x02 // 1 bits
private const val TEMPERATURE_TYPE_FLAG: Byte = 0x04 // 1 bit

internal object HTSTemperatureMeasurementParser {

    fun parse(data: Data): String {
        var offset = 0
        val flags = data.getIntValue(Data.FORMAT_UINT8, offset++)!!

        /*
		 * false 	Temperature is in Celsius degrees 
		 * true 	Temperature is in Fahrenheit degrees 
		 */
        val fahrenheit = flags and TEMPERATURE_UNIT_FLAG.toInt() > 0

        /*
		 * false 	No Timestamp in the packet
		 * true 	There is a timestamp information
		 */
        val timestampIncluded = flags and TIMESTAMP_FLAG.toInt() > 0

        /*
		 * false 	Temperature type is not included
		 * true 	Temperature type included in the packet
		 */
        val temperatureTypeIncluded = flags and TEMPERATURE_TYPE_FLAG.toInt() > 0
        val tempValue = data.getFloatValue(Data.FORMAT_FLOAT, offset)!!
        offset += 4
        var dateTime: String? = null
        if (timestampIncluded) {
            dateTime = HTSDateTimeParser.parse(data, offset)
            offset += 7
        }
        var type: String? = null
        if (temperatureTypeIncluded) {
            type = HTSTemperatureTypeParser.parse(data, offset)
            // offset++;
        }
        val builder = StringBuilder()
        builder.append(String.format(Locale.US, "%.02f", tempValue))
        if (fahrenheit) builder.append("°F") else builder.append("°C")
        if (timestampIncluded) builder.append("\nTime: ").append(dateTime)
        if (temperatureTypeIncluded) builder.append("\nType: ").append(type)
        return builder.toString()
    }
}
