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
import android.ranging.SensorFusionParams
import android.ranging.SessionConfig
import android.ranging.ble.cs.BleCsRangingParams
import android.ranging.raw.RawRangingDevice
import android.ranging.raw.RawResponderRangingConfig
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
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
            Timber.tag("BBB").d(
                "RangingManager session closed with reason: ${
                    RangingSessionCloseReason.getReason(reason)
                }"
            )
        }

        override fun onOpenFailed(reason: Int) {
            Timber.tag("BBB")
                .d(
                    "RangingManager session open failed with reason: ${
                        RangingSessionFailedReason.getReason(reason)
                    }"
                )
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
            Timber.tag("BBB").d("onResults rangingTechnology: ${data.rangingTechnology}")
            Timber.tag("BBB").d(
                "onResults azimuth: ${data.azimuth}\televation: ${data.elevation}\tpeer: ${peer.uuid}\tdistance ${data.distance}\t" +
                        " rssi: ${data.rssi} \tmeasurement: $measurement\tconfidence: $confidence"
            )
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
            // RangingManager is not supported on this device
            return
        }
        val rangingCapabilityCallback = RangingManager.RangingCapabilitiesCallback { capabilities ->
            if (capabilities.csCapabilities != null) {
                capabilities.csCapabilities!!.supportedSecurityLevels
                    .find { it == 1 }
                    ?.let {
                        Timber.d("Channel Sounding supported.")
                    }
            } else {
                Timber.d("CS Capabilities is not supported")
            }

        }

        rangingManager.registerCapabilitiesCallback(
            context.mainExecutor,
            rangingCapabilityCallback
        )

        val rangingDevice = RangingDevice.Builder()
            .build()

        val csRangingParams = BleCsRangingParams.Builder(device)
            .build()

        val rawRangingDevice = RawRangingDevice.Builder()
            .setRangingDevice(rangingDevice)
            .setCsRangingParams(csRangingParams)
            .build()

        val rawRangingDeviceConfig = RawResponderRangingConfig.Builder()
            .setRawRangingDevice(rawRangingDevice)
            .build()

        val rangingPreference = RangingPreference.Builder(
            DEVICE_ROLE_RESPONDER,
            rawRangingDeviceConfig
        )
            .setSessionConfig(
                SessionConfig.Builder()
                    .setRangingMeasurementsLimit(1000)
                    .setAngleOfArrivalNeeded(true)
                    .setSensorFusionParams(
                        SensorFusionParams.Builder()
                            .setSensorFusionEnabled(false)
                            .build()
                    )
                    .build()
            )
            .build()

        rangingSession = rangingManager.createRangingSession(
            context.mainExecutor,
            rangingSessionCallback
        )
        rangingSession?.let {
            it.addDeviceToRangingSession(rawRangingDeviceConfig)
            it.start(rangingPreference)
        }
    }

}

