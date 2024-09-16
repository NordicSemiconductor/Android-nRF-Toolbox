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
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import java.util.Calendar

internal object DateTimeParser {

    fun parse(bytes: DataByteArray, offset: Int): Calendar? {
        if (bytes.size < offset + 7) return null
        val calendar = Calendar.getInstance()
        val year: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
        val month: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 2) ?: return null
        val day: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 3) ?: return null
        val hourOfDay: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 4) ?: return null
        val minute: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 5) ?: return null
        val second: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 6) ?: return null

        if (year > 0) {
            calendar[Calendar.YEAR] = year
        } else {
            calendar.clear(Calendar.YEAR)
        }

        if (month > 0) {
            calendar[Calendar.MONTH] = month - 1
        } else {
            calendar.clear(Calendar.MONTH)
        }

        if (day > 0) {
            calendar[Calendar.DATE] = day
        } else {
            calendar.clear(Calendar.DATE)
        }

        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
        calendar[Calendar.MINUTE] = minute
        calendar[Calendar.SECOND] = second
        calendar[Calendar.MILLISECOND] = 0

        return calendar
    }
}
