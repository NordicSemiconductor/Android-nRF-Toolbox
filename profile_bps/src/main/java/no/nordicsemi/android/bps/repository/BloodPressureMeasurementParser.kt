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
package no.nordicsemi.android.bps.repository

import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.bps.repository.DateTimeParser.parse
import java.lang.StringBuilder

object BloodPressureMeasurementParser {

    fun parse(data: Data): String {
        val builder = StringBuilder()

        // first byte - flags
        var offset = 0
        val flags = data.getIntValue(Data.FORMAT_UINT8, offset++)!!
        val unitType = flags and 0x01
        val timestampPresent = flags and 0x02 > 0
        val pulseRatePresent = flags and 0x04 > 0
        val userIdPresent = flags and 0x08 > 0
        val statusPresent = flags and 0x10 > 0

        // following bytes - systolic, diastolic and mean arterial pressure 
        val systolic = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
        val diastolic = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 2)!!
        val meanArterialPressure = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 4)!!
        val unit = if (unitType == 0) " mmHg" else " kPa"
        offset += 6
        builder.append("Systolic: ").append(systolic).append(unit)
        builder.append("\nDiastolic: ").append(diastolic).append(unit)
        builder.append("\nMean AP: ").append(meanArterialPressure).append(unit)

        // parse timestamp if present
        if (timestampPresent) {
            builder.append("\nTimestamp: ").append(parse(data, offset))
            offset += 7
        }

        // parse pulse rate if present
        if (pulseRatePresent) {
            val pulseRate = data.getFloatValue(Data.FORMAT_SFLOAT, offset)!!
            offset += 2
            builder.append("\nPulse: ").append(pulseRate).append(" bpm")
        }
        if (userIdPresent) {
            val userId = data.getIntValue(Data.FORMAT_UINT8, offset)!!
            offset += 1
            builder.append("\nUser ID: ").append(userId)
        }
        if (statusPresent) {
            val status = data.getIntValue(Data.FORMAT_UINT16, offset)!!
            // offset += 2;
            if (status and 0x0001 > 0) builder.append("\nBody movement detected")
            if (status and 0x0002 > 0) builder.append("\nCuff too lose")
            if (status and 0x0004 > 0) builder.append("\nIrregular pulse detected")
            if (status and 0x0018 == 0x0008) builder.append("\nPulse rate exceeds upper limit")
            if (status and 0x0018 == 0x0010) builder.append("\nPulse rate is less than lower limit")
            if (status and 0x0018 == 0x0018) builder.append("\nPulse rate range: Reserved for future use ")
            if (status and 0x0020 > 0) builder.append("\nImproper measurement position")
        }
        return builder.toString()
    }
}
