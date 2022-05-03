package no.nordicsemi.android.prx.repository

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.prx.data.AlarmLevel
import javax.inject.Inject

internal class AlarmHandler @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val ringtone = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))

    fun playAlarm(alarmLevel: AlarmLevel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.volume = when (alarmLevel) {
                AlarmLevel.NONE -> 0f
                AlarmLevel.MEDIUM -> 0.5f
                AlarmLevel.HIGH -> 1f
            }
        }

        ringtone.play()
    }

    fun pauseAlarm() {
        if (ringtone.isPlaying) {
            ringtone.stop()
        }
    }
}
