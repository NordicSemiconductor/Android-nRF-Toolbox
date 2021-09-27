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
package no.nordicsemi.android.hrs.service

import no.nordicsemi.android.ble.data.Data
import java.util.*

object HeartRateMeasurementParser {

    private const val HEART_RATE_VALUE_FORMAT: Byte = 0x01 // 1 bit
    private const val SENSOR_CONTACT_STATUS: Byte = 0x06 // 2 bits
    private const val ENERGY_EXPANDED_STATUS: Byte = 0x08 // 1 bit
    private const val RR_INTERVAL: Byte = 0x10 // 1 bit

    fun parse(data: Data): String {
        var offset = 0
        val flags = data.getIntValue(Data.FORMAT_UINT8, offset++)!!

        /*
		 * false 	Heart Rate Value Format is set to UINT8. Units: beats per minute (bpm) 
		 * true 	Heart Rate Value Format is set to UINT16. Units: beats per minute (bpm)
		 */
        val value16bit = flags and HEART_RATE_VALUE_FORMAT.toInt() > 0

        /*
		 * 0 	Sensor Contact feature is not supported in the current connection
		 * 1 	Sensor Contact feature is not supported in the current connection
		 * 2 	Sensor Contact feature is supported, but contact is not detected
		 * 3 	Sensor Contact feature is supported and contact is detected
		 */
        val sensorContactStatus = flags and SENSOR_CONTACT_STATUS.toInt() shr 1

        /*
		 * false 	Energy Expended field is not present
		 * true 	Energy Expended field is present. Units: kilo Joules
		 */
        val energyExpandedStatus = flags and ENERGY_EXPANDED_STATUS.toInt() > 0

        /*
		 * false 	RR-Interval values are not present.
		 * true 	One or more RR-Interval values are present. Units: 1/1024 seconds
		 */
        val rrIntervalStatus = flags and RR_INTERVAL.toInt() > 0

        // heart rate value is 8 or 16 bit long
        val heartRateValue = data.getIntValue(
            if (value16bit) {
                Data.FORMAT_UINT16
            } else {
                Data.FORMAT_UINT8
            },
            offset++
        ) // bits per minute
        if (value16bit) offset++

        // energy expanded value is present if a flag was set
        var energyExpanded = -1
        if (energyExpandedStatus) energyExpanded = data.getIntValue(Data.FORMAT_UINT16, offset)!!
        offset += 2

        // RR-interval is set when a flag is set
        val rrIntervals: MutableList<Float> = ArrayList()
        if (rrIntervalStatus) {
            var o = offset
            while (o < data.value!!.size) {
                val units = data.getIntValue(Data.FORMAT_UINT16, o)!!
                rrIntervals.add(units * 1000.0f / 1024.0f) // RR interval is in [1/1024s]
                o += 2
            }
        }
        val builder = StringBuilder()
        builder.append("Heart Rate Measurement: ").append(heartRateValue).append(" bpm")
        when (sensorContactStatus) {
            0, 1 -> builder.append(",\nSensor Contact Not Supported")
            2 -> builder.append(",\nContact is NOT Detected")
            3 -> builder.append(",\nContact is Detected")
        }
        if (energyExpandedStatus) {
            builder.append(",\nEnergy Expanded: ")
                .append(energyExpanded)
                .append(" kJ")
        }
        if (rrIntervalStatus) {
            builder.append(",\nRR Interval: ")
            for (interval in rrIntervals) builder.append(
                String.format(
                    Locale.US,
                    "%.02f ms, ",
                    interval
                )
            )
            builder.setLength(builder.length - 2) // remove the ", " at the end
        }
        return builder.toString()
    }
}