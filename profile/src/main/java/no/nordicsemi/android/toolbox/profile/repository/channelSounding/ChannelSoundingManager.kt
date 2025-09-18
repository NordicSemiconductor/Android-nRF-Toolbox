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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.toolbox.profile.data.RangingSessionAction
import no.nordicsemi.android.toolbox.profile.data.UpdateRate
import timber.log.Timber

object ChannelSoundingManager {
    private val _rangingData = MutableStateFlow<RangingSessionAction?>(null)
    val rangingData = _rangingData.asStateFlow()

    private var rangingSession: RangingSession? = null

    private val rangingSessionCallback = @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    object : RangingSession.Callback {
        override fun onClosed(reason: Int) {
            _rangingData.value =
                RangingSessionAction.OnError(RangingSessionCloseReason.getReason(reason))
        }

        override fun onOpenFailed(reason: Int) {
            _rangingData.value =
                RangingSessionAction.OnError(RangingSessionFailedReason.getReason(reason))
        }

        override fun onOpened() {
            _rangingData.value = RangingSessionAction.OnStart
        }

        override fun onResults(
            peer: RangingDevice,
            data: RangingData
        ) {
            _rangingData.value = RangingSessionAction.OnResult(data)
        }

        override fun onStarted(
            peer: RangingDevice,
            technology: Int
        ) {
            _rangingData.value = RangingSessionAction.OnStart
        }

        override fun onStopped(
            peer: RangingDevice,
            technology: Int
        ) {
            _rangingData.value = RangingSessionAction.OnClosed
        }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun addDeviceToRangingSession(
        context: Context,
        device: String,
        updateRate: UpdateRate = UpdateRate.NORMAL
    ) {
        val rangingManager = try {
            context.getSystemService(RangingManager::class.java)
        } catch (_: Exception) {
            null
        }
        if (rangingManager == null) {
            _rangingData.value = RangingSessionAction.OnError("RangingManager is not available")
            return
        }
        val setRangingUpdateRate = when (updateRate) {
            UpdateRate.FREQUENT -> RawRangingDevice.UPDATE_RATE_FREQUENT
            UpdateRate.NORMAL -> RawRangingDevice.UPDATE_RATE_NORMAL
            UpdateRate.INFREQUENT -> RawRangingDevice.UPDATE_RATE_INFREQUENT
        }
        val rangingDevice = RangingDevice.Builder()
            .build()

        val csRangingParams = BleCsRangingParams
            .Builder(device)
            .setRangingUpdateRate(setRangingUpdateRate)
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

        val rangingCapabilityCallback = RangingManager.RangingCapabilitiesCallback { capabilities ->
            if (capabilities.csCapabilities != null) {
                if (capabilities.csCapabilities!!.supportedSecurityLevels.contains(1)) {
                    // Channel Sounding supported
                    // Start the Channel Sounding session
                    rangingSession = rangingManager.createRangingSession(
                        context.mainExecutor,
                        rangingSessionCallback
                    )
                    rangingSession?.let {
                        try {
                            it.addDeviceToRangingSession(rawRangingDeviceConfig)
                        } catch (e: Exception) {
                            Timber.e("Failed to add device to ranging session: ${e.message}")
                            _rangingData.value = RangingSessionAction.OnClosed
                        } finally {
                            it.start(rangingPreference)
                        }
                    }
                } else {
                    _rangingData.value =
                        RangingSessionAction.OnError("Channel Sounding with required security level is not supported")
                    closeSession()
                }
            } else {
                _rangingData.value =
                    RangingSessionAction.OnError("Channel Sounding Capabilities is not supported")
                closeSession()
            }

        }

        rangingManager.registerCapabilitiesCallback(
            context.mainExecutor,
            rangingCapabilityCallback
        )
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun closeSession(onClosed: (suspend () -> Unit)? = null) {
        try {
            rangingSession?.let { session ->
                session.stop()
                session.close()
                rangingSession = null
                _rangingData.value = null
                // unregister the callback

                onClosed?.let {
                    _rangingData.value = RangingSessionAction.OnStart
                    // Wait for a moment to ensure the session is properly closed before invoking the callback
                    // Launch a coroutine to delay and call onClosed
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(500)
                        it()
                    }

                }
            }
        } catch (e: Exception) {
            _rangingData.value = RangingSessionAction.OnError(e.message ?: "Unknown error")
        }
    }

}

