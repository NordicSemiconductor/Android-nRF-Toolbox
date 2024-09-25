/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.toolbox.libs.profile.data.hts.data

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import java.util.Calendar

object HTSDataParser {

    fun parse(byte: ByteArray): HtsData? {
        val bytes = DataByteArray(byte)

        if (bytes.size < 5) {
            return null
        }

        var offset = 0
        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null

        val unit: TemperatureUnitData = TemperatureUnitData.create(flags and 0x01) ?: return null

        val timestampPresent = flags and 0x02 != 0
        val temperatureTypePresent = flags and 0x04 != 0
        offset += 1

        if (bytes.size < 5 + (if (timestampPresent) 7 else 0) + (if (temperatureTypePresent) 1 else 0)) {
            return null
        }

        val temperature: Float =
            bytes.getFloatValue(FloatFormat.FORMAT_FLOAT, offset) ?: return null
        offset += 4

        var calendar: Calendar? = null
        if (timestampPresent) {
            calendar = DateTimeParser.parse(bytes, offset)
            offset += 7
        }

        var type: Int? = null
        if (temperatureTypePresent) {
            type = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset)
            // offset += 1;
        }

        return HtsData(temperature, unit, calendar, type)
    }
}