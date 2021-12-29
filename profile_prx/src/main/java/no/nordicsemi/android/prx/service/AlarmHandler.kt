package no.nordicsemi.android.prx.service

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

class AlarmHandler @Inject constructor(
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

    fun playAlarm() {
        val am = context.getSystemService(LifecycleService.AUDIO_SERVICE) as AudioManager
        originalVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
        am.setStreamVolume(
            AudioManager.STREAM_ALARM,
            am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )
        try {
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: IOException) {
            Log.e(TAG, "Prepare Alarm failed: ", e)
        }
    }

    fun pauseAlarm() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            // Restore original volume
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
        }
    }

    fun releaseAlarm() {
        mediaPlayer.release()
    }
}
