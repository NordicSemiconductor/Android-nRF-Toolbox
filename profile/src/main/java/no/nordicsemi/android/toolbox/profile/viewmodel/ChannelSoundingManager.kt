package no.nordicsemi.android.toolbox.profile.viewmodel

import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ChannelSoundingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val rangingManager =
        if (Build.VERSION.SDK_INT >= 36) context.getSystemService(RangingManager::class.java) else null

    private var rangingSession: RangingSession? = null

    private val rangingSessionCallback = @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    object : RangingSession.Callback {
        override fun onClosed(reason: Int) {
            Timber.tag("AAA").d("RangingManager session closed with reason: $reason")
        }

        override fun onOpenFailed(reason: Int) {
            Timber.tag("AAA")
                .d("RangingManager session open failed with reason: $reason")
        }

        override fun onOpened() {
            Timber.tag("AAA").d("RangingManager session opened successfully.")
        }

        override fun onResults(
            peer: RangingDevice,
            data: RangingData
        ) {
            Timber.tag("AAA").d("distance ${data.distance}\n angle: ${data.rssi} ")
        }

        override fun onStarted(
            peer: RangingDevice,
            technology: Int
        ) {
            Timber.tag("AAA")
                .d("RangingManager session started with peer: ${peer.uuid}, technology: $technology")
        }

        override fun onStopped(
            peer: RangingDevice,
            technology: Int
        ) {
            Timber.tag("AAA").d("RangingManager session stopped with peer: ${peer.uuid}")
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
        Timber.tag("AAA").d("rangingDevice: $rangingDevice")
        val bleCsRangingParams = BleCsRangingParams.Builder(device).build()
        Timber.tag("AAA").d("bleCsRangingParams: $bleCsRangingParams")
        val rawRangingDevice = RawRangingDevice.Builder()
            .setCsRangingParams(bleCsRangingParams)
            .setRangingDevice(rangingDevice)
            .build()

        Timber.tag("AAA").d("rawRangingDevice: $rawRangingDevice")

        val rawResponderRangingConfig = RawResponderRangingConfig.Builder()
            .setRawRangingDevice(rawRangingDevice)
            .build()

        Timber.tag("AAA").d("rawResponderRangingConfig: $rawResponderRangingConfig")
        val rangingPreference = RangingPreference.Builder(
            DEVICE_ROLE_RESPONDER,
            rawResponderRangingConfig
        ).build()
        Timber.tag("AAA").d("rangingPreference: $rangingPreference")


        rangingSession = rangingManager.createRangingSession(
            context.mainExecutor,
            rangingSessionCallback
        )
        Timber.tag("AAA").d("rangingSessionCallback: $rangingSessionCallback")
        // Check permission, though it should be granted automatically.
        if (ContextCompat.checkSelfPermission(
                context,
                "android.permission.RANGING"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.tag("AAA").e("Missing android.permission.RANGING — cannot start ranging")
            return
        }

        // If the session is null, it means that the RangingManager is not available.
        if (rangingSession == null) {
            Timber.tag("AAA").e("RangingManager session is null")
            return
        }
        rangingSession!!.addDeviceToRangingSession(rawResponderRangingConfig)
        rangingSession!!.start(rangingPreference)
    }

}

