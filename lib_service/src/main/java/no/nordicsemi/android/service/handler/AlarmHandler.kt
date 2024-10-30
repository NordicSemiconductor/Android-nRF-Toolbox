package no.nordicsemi.android.service.handler

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.toolbox.libs.core.data.prx.AlarmLevel
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
