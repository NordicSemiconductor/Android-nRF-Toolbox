package no.nordicsemi.android.toolbox.profile.repository.channelSounding

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.ranging.RangingData
import android.ranging.RangingDevice
import android.ranging.RangingManager
import android.ranging.RangingPreference
import android.ranging.RangingPreference.DEVICE_ROLE_RESPONDER
import android.ranging.RangingSession
import android.ranging.ble.cs.BleCsRangingParams
import android.ranging.raw.RawRangingDevice
import android.ranging.raw.RawResponderRangingConfig
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.toolbox.profile.repository.channelSounding.RangingSessionCloseReason.Companion.getReason
import no.nordicsemi.android.toolbox.profile.repository.channelSounding.RangingSessionStartTechnology.Companion.getTechnology
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("NewApi")
@Singleton
internal class ChannelSoundingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val rangingManager =
        if (Build.VERSION.SDK_INT >= 36) context.getSystemService(RangingManager::class.java) else null

    private var rangingSession: RangingSession? = null

    private val rangingSessionCallback = object : RangingSession.Callback {
        override fun onClosed(reason: Int) {
            Timber.tag("BBB").d("RangingManager session closed with reason: ${getReason(reason)}")
        }

        override fun onOpenFailed(reason: Int) {
            Timber.tag("BBB")
                .d("RangingManager session open failed with reason: $reason")
        }

        override fun onOpened() {
            Timber.tag("BBB").d("RangingManager session opened successfully.")
        }

        override fun onResults(
            peer: RangingDevice,
            data: RangingData
        ) {
            val measurement = data.distance?.measurement
            val confidence = data.distance?.confidence
            Timber.tag("BBB").d("RangingManager session results with peer: ${peer.uuid}")
            Timber.tag("BBB").d("distance ${data.distance}\n angle: ${data.rssi} ")
            Timber.tag("BBB").d("measurement: $measurement")
            Timber.tag("BBB").d("confidence: $confidence")
        }

        override fun onStarted(
            peer: RangingDevice,
            technology: Int
        ) {
            Timber.tag("BBB")
                .d(
                    "RangingManager session started with peer: ${peer.uuid}, " +
                            "\ntechnology: ${getTechnology(technology)}"
                )
        }

        override fun onStopped(
            peer: RangingDevice,
            technology: Int
        ) {
            Timber.tag("BBB").d("RangingManager session stopped with peer: ${peer.uuid}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun addDeviceToRangingSession(
        device: String
    ) {
        if (rangingManager == null) {
            Timber.tag("AAA").d("RangingManager is not available")
            return
        }
        val rangingDevice = RangingDevice.Builder().build()

        val bleCsRangingParams = BleCsRangingParams.Builder(device).build()

        val rawRangingDevice = RawRangingDevice.Builder()
            .setCsRangingParams(bleCsRangingParams)
            .setRangingDevice(rangingDevice)
            .build()

        val rawResponderRangingConfig = RawResponderRangingConfig.Builder()
            .setRawRangingDevice(rawRangingDevice)
            .build()

        val rangingPreference = RangingPreference.Builder(
            DEVICE_ROLE_RESPONDER,
            rawResponderRangingConfig
        ).build()

        Timber.tag("AAA").d("rangingPreference: $rangingPreference")
        Timber.tag("AAA").d("rawResponderRangingConfig: $rawResponderRangingConfig")
        Timber.tag("AAA").d("rawRangingDevice: $rawRangingDevice")
        Timber.tag("AAA").d("bleCsRangingParams: $bleCsRangingParams")
        Timber.tag("AAA").d("rangingSessionCallback: $rangingSessionCallback")
        Timber.tag("AAA").d("rangingDevice: $rangingDevice")

        rangingSession = rangingManager.createRangingSession(
            context.mainExecutor,
            rangingSessionCallback
        )
        if (rangingSession == null) {
            Timber.tag("AAA").d("RangingManager session is null")
            return
        }
        /* // first remove the device if it exists
         rangingSession!!.removeDeviceFromRangingSession(rangingDevice)
         sleep(2000)*/
        rangingSession!!.addDeviceToRangingSession(rawResponderRangingConfig)
        rangingSession!!.start(rangingPreference)
    }

}

