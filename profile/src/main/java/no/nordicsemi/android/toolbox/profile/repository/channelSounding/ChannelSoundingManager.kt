package no.nordicsemi.android.toolbox.profile.repository.channelSounding

import android.content.Context
import android.os.Build
import android.ranging.RangingData
import android.ranging.RangingDevice
import android.ranging.RangingManager
import android.ranging.RangingPreference
import android.ranging.RangingPreference.DEVICE_ROLE_INITIATOR
import android.ranging.RangingSession
import android.ranging.SensorFusionParams
import android.ranging.SessionConfig
import android.ranging.ble.cs.BleCsRangingCapabilities
import android.ranging.ble.cs.BleCsRangingParams
import android.ranging.raw.RawRangingDevice
import android.ranging.raw.RawResponderRangingConfig
import androidx.annotation.RequiresApi
import no.nordicsemi.android.toolbox.profile.repository.channelSounding.RangingSessionStartTechnology.Companion.getTechnology
import timber.log.Timber

object ChannelSoundingManager {

    private var rangingSession: RangingSession? = null

    private val rangingSessionCallback = @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    object : RangingSession.Callback {
        override fun onClosed(reason: Int) {
            Timber.d("closed, reason: ${RangingSessionCloseReason.getReason(reason)}")
        }

        override fun onOpenFailed(reason: Int) {
            Timber.d("Failed, reason: ${RangingSessionFailedReason.getReason(reason)}")
        }

        override fun onOpened() {
            Timber.d("Opened successfully.")
        }

        override fun onResults(
            peer: RangingDevice,
            data: RangingData
        ) {
            val measurement = data.distance?.measurement
            val confidence = data.distance?.confidence
            Timber.d("RangingTechnology: ${data.rangingTechnology}")
            Timber.d("Distance: ${if (measurement != null) "$measurement m" else "null"}")
            Timber.d("Confidence: ${if (confidence != null) "$confidence %" else "null"}")
            Timber.d(
                "\nAzimuth: ${data.azimuth}\nelevation: " +
                        "${data.elevation}\npeer: ${peer.uuid}"
            )
        }

        override fun onStarted(
            peer: RangingDevice,
            technology: Int
        ) {
            Timber.d(
                "Session started with peer: ${peer.uuid}, \ntechnology: ${getTechnology(technology)}"
            )
        }

        override fun onStopped(
            peer: RangingDevice,
            technology: Int
        ) {
            Timber.d("Session stopped with peer: ${peer.uuid}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun addDeviceToRangingSession(
        context: Context,
        device: String
    ) {
        val rangingManager = try {
            context.getSystemService(RangingManager::class.java)
        } catch (_: Exception) {
            null
        }
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
                Timber.d("Channel Sounding Capabilities is not supported")
            }

        }

        rangingManager.registerCapabilitiesCallback(
            context.mainExecutor,
            rangingCapabilityCallback
        )

        val rangingDevice = RangingDevice.Builder()
            .build()

        val csRangingParams = BleCsRangingParams
            .Builder(device)
            .setRangingUpdateRate(RawRangingDevice.UPDATE_RATE_INFREQUENT)
            .setSecurityLevel(BleCsRangingCapabilities.CS_SECURITY_LEVEL_ONE)
            .build()

        val rawRangingDevice = RawRangingDevice.Builder()
            .setRangingDevice(rangingDevice)
            .setCsRangingParams(csRangingParams)
            .build()

        val rawRangingDeviceConfig = RawResponderRangingConfig.Builder()
            .setRawRangingDevice(rawRangingDevice)
            .build()

        val rangingPreference = RangingPreference.Builder(
            DEVICE_ROLE_INITIATOR,
            rawRangingDeviceConfig
        )
            .setSessionConfig(
                SessionConfig.Builder()
                    .setRangingMeasurementsLimit(1000)
                    .setAngleOfArrivalNeeded(true)
                    .setSensorFusionParams(
                        SensorFusionParams.Builder()
                            .setSensorFusionEnabled(true)
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

