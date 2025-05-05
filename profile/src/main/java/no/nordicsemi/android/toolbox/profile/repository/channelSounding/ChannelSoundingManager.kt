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
import android.ranging.oob.DeviceHandle
import android.ranging.oob.OobResponderRangingConfig
import android.ranging.oob.TransportHandle
import android.ranging.raw.RawRangingDevice
import android.ranging.raw.RawResponderRangingConfig
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.toolbox.profile.repository.channelSounding.RangingSessionStartTechnology.Companion.getTechnology
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

val CHANNEL_SOUND_SERVICE_UUID: UUID = UUID.fromString("0000185B-0000-1000-8000-00805F9B34FB")

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
            Timber.tag("BBB").d("onResults azimuth: ${data.azimuth}")
            Timber.tag("BBB").d("onResults elevation: ${data.elevation}")
            Timber.tag("BBB").d("RangingManager session results with peer: ${peer.uuid}")
            Timber.tag("BBB").d("distance ${data.distance}\n rssi: ${data.rssi} ")
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

    @OptIn(ExperimentalStdlibApi::class)
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun addDeviceToRangingSession(
        device: String
    ) {
        if (rangingManager == null) {
            Timber.tag("AAA").d("RangingManager is not available")
            return
        }
        val rangingCapabilityCallback = RangingManager.RangingCapabilitiesCallback {
            if (it.csCapabilities != null) {
                Timber.tag("CCC").d("CS Capabilities: ${it.csCapabilities}")
                it.csCapabilities!!.supportedSecurityLevels
            } else {
                Timber.tag("CCC").d("CS Capabilities is not supported")
            }

        }
        rangingManager.registerCapabilitiesCallback(
            context.mainExecutor,
            rangingCapabilityCallback
        )

        val rangingDevice = RangingDevice.Builder()
            .build()


        val transportHandle = object : TransportHandle {
            override fun close() {
                Timber.tag("AAA").d("TransportHandle close")
            }

            override fun registerReceiveCallback(
                executor: Executor,
                callback: TransportHandle.ReceiveCallback
            ) {
                Timber.tag("AAA").d("TransportHandle registerReceiveCallback")
            }

            override fun sendData(data: ByteArray) {
                Timber.tag("AAA").d("TransportHandle sendData: ${data.toHexString()}")
            }
        }

        val oobResponderRangingConfig = OobResponderRangingConfig.Builder(
            DeviceHandle.Builder(rangingDevice, transportHandle).build()
        )
            .build()

        val rawRangingDevice = RawRangingDevice.Builder()
            .setCsRangingParams(BleCsRangingParams.Builder(device).build())
            .setRangingDevice(rangingDevice).build()

        val rawRangingDeviceConfig =
            RawResponderRangingConfig.Builder().setRawRangingDevice(rawRangingDevice).build()

        val rangingPreference = RangingPreference.Builder(
            DEVICE_ROLE_RESPONDER,
            rawRangingDeviceConfig
        ).build()

        Timber.tag("AAA").d("rangingSessionCallback: $rangingSessionCallback")
        Timber.tag("AAA").d("rangingPreference: $rangingPreference")
        Timber.tag("AAA").d("rangingDevice: $rangingDevice")

        if (rangingSession != null) {
            Timber.tag("AAA").d("RangingManager session is already started")
            return
        }
        rangingSession = rangingManager.createRangingSession(
            context.mainExecutor,
            rangingSessionCallback
        )

        if (rangingSession == null) {
            Timber.tag("AAA").d("RangingManager session is null")
            return
        }
        rangingSession!!.addDeviceToRangingSession(rawRangingDeviceConfig)
        rangingSession!!.start(rangingPreference)
    }

}

