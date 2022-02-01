package no.nordicsemi.android.prx.repository

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.prx.data.AlarmLevel
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

internal class AlarmHandler @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val TAG = "ALARM_MANAGER"

    private var mediaPlayer = MediaPlayer()
    private var originalVolume = 0

    init {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(1.0f, 1.0f)
        try {
            mediaPlayer.setDataSource(
                context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )
        } catch (e: IOException) {
            Log.e(TAG, "Initialize Alarm failed: ", e)
        }
    }

    fun playAlarm(alarmLevel: AlarmLevel) {
        val am = context.getSystemService(LifecycleService.AUDIO_SERVICE) as AudioManager
        originalVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)

        val soundLevel = when (alarmLevel) {
            AlarmLevel.NONE -> 0
            AlarmLevel.MEDIUM -> originalVolume / 2
            AlarmLevel.HIGH -> originalVolume
        }

        am.setStreamVolume(
            AudioManager.STREAM_ALARM,
            soundLevel,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )
        try {
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            Log.e(TAG, "Prepare Alarm failed: ", e)
        }
    }

    fun pauseAlarm() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                // Restore original volume
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                am.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Prepare Alarm failed: ", e)
        }
    }

    fun releaseAlarm() {
        mediaPlayer.release()
    }
}
