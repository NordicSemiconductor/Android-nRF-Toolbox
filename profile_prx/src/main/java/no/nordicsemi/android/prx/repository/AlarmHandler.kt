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

package no.nordicsemi.android.prx.repository

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.kotlin.ble.profile.prx.AlarmLevel
import javax.inject.Inject

internal class AlarmHandler @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val highLevelRingtone = RingtoneManager
        .getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        ?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                volume = 1f
            }
        }

    private val mediumLevelRingtone = RingtoneManager
        .getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
        ?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                volume = 0.5f
            }
        }

    fun playAlarm(alarmLevel: AlarmLevel) {
        if (alarmLevel == AlarmLevel.NONE) {
            pauseAlarm()
            return
        }
        val ringtone = when (alarmLevel) {
            AlarmLevel.NONE -> null
            AlarmLevel.MEDIUM -> mediumLevelRingtone
            AlarmLevel.HIGH -> highLevelRingtone
        }
        ringtone?.play()
    }

    fun pauseAlarm() {
        highLevelRingtone?.takeIf { it.isPlaying }?.stop()
        mediumLevelRingtone?.takeIf { it.isPlaying }?.stop()
    }
}
