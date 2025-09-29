package no.nordicsemi.android.toolbox.profile.repository.channelSounding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import android.ranging.raw.RawInitiatorRangingConfig
import android.ranging.raw.RawRangingDevice
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.toolbox.profile.data.RangingSessionAction
import no.nordicsemi.android.toolbox.profile.data.RangingSessionFailedReason
import no.nordicsemi.android.toolbox.profile.data.SessionClosedReason
import no.nordicsemi.android.toolbox.profile.data.UpdateRate
import no.nordicsemi.android.toolbox.profile.view.channelSounding.toCsRangingData
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Singleton
class ChannelSoundingManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val rangingManager: RangingManager? =
        context.getSystemService(RangingManager::class.java)
    private lateinit var rangingCapabilityCallback: RangingManager.RangingCapabilitiesCallback

    private val _rangingData = MutableStateFlow<RangingSessionAction?>(null)
    val rangingData = _rangingData.asStateFlow()
    private val _previousRangingDataList = MutableStateFlow<List<Float>>(emptyList())

    private var rangingSession: RangingSession? = null

    private val rangingSessionCallback = @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    object : RangingSession.Callback {
        override fun onClosed(reason: Int) {
            _rangingData.value =
                RangingSessionAction.OnError(RangingSessionFailedReason.getReason(reason))
            // Unregister the callback to avoid memory leaks
            rangingManager?.unregisterCapabilitiesCallback(rangingCapabilityCallback)
            // Cleanup previous data
            _previousRangingDataList.value = emptyList()
        }

        override fun onOpenFailed(reason: Int) {
            _rangingData.value =
                RangingSessionAction.OnError(RangingSessionFailedReason.getReason(reason))
            // Unregister the callback to avoid memory leaks
            rangingManager?.unregisterCapabilitiesCallback(rangingCapabilityCallback)
            // Cleanup previous data
            _previousRangingDataList.value = emptyList()
        }

        override fun onOpened() {
            _rangingData.value = RangingSessionAction.OnStart
        }

        override fun onResults(
            peer: RangingDevice,
            data: RangingData
        ) {
            val updatedList = _previousRangingDataList.value.toMutableList()
            data.distance?.measurement?.let {
                updatedList.add(it.toFloat())
            }
            _previousRangingDataList.value = updatedList
            _rangingData.value = RangingSessionAction.OnResult(
                data = data.toCsRangingData(),
                previousData = _previousRangingDataList.value
            )
        }

        override fun onStarted(
            peer: RangingDevice,
            technology: Int
        ) {
            _rangingData.value = RangingSessionAction.OnStart
            // Cleanup previous data
            _previousRangingDataList.value = emptyList()
        }

        override fun onStopped(
            peer: RangingDevice,
            technology: Int
        ) {
            _rangingData.value = RangingSessionAction.OnClosed
            // Cleanup previous data
            _previousRangingDataList.value = emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun addDeviceToRangingSession(
        device: String,
        updateRate: UpdateRate = UpdateRate.NORMAL
    ) {
        if (rangingManager == null) {
            _rangingData.value =
                RangingSessionAction.OnError(SessionClosedReason.RANGING_NOT_AVAILABLE)
            return
        }
        val setRangingUpdateRate = when (updateRate) {
            UpdateRate.FREQUENT -> RawRangingDevice.UPDATE_RATE_FREQUENT
            UpdateRate.NORMAL -> RawRangingDevice.UPDATE_RATE_NORMAL
            UpdateRate.INFREQUENT -> RawRangingDevice.UPDATE_RATE_INFREQUENT
        }
        val rangingDevice = RangingDevice.Builder()
            .setUuid(UUID.nameUUIDFromBytes(device.toByteArray()))
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

        val rawRangingDeviceConfig = RawInitiatorRangingConfig.Builder()
            .addRawRangingDevice(rawRangingDevice)
            .build()

        val sensorFusionParams = SensorFusionParams.Builder()
            .setSensorFusionEnabled(true)
            .build()

        val sessionConfig = SessionConfig.Builder()
            .setRangingMeasurementsLimit(1000)
            .setAngleOfArrivalNeeded(true)
            .setSensorFusionParams(sensorFusionParams)
            .build()


        val rangingPreference = RangingPreference.Builder(
            DEVICE_ROLE_INITIATOR,
            rawRangingDeviceConfig
        )
            .setSessionConfig(sessionConfig)
            .build()

        rangingCapabilityCallback = RangingManager.RangingCapabilitiesCallback { capabilities ->
            if (capabilities.csCapabilities != null) {
                if (capabilities.csCapabilities!!.supportedSecurityLevels.contains(1)) {
                    // Channel Sounding supported
                    // Check if Ranging Permission is granted before starting the session
                    if (hasRangingPermissions(context)) {
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
                            RangingSessionAction.OnError(
                                SessionClosedReason.MISSING_PERMISSION
                            )
                        return@RangingCapabilitiesCallback
                    }
                } else {
                    _rangingData.value =
                        RangingSessionAction.OnError(SessionClosedReason.CS_SECURITY_NOT_AVAILABLE)
                    closeSession()
                }
            } else {
                _rangingData.value =
                    RangingSessionAction.OnError(SessionClosedReason.NOT_SUPPORTED)
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
                rangingManager?.unregisterCapabilitiesCallback(rangingCapabilityCallback)
                // Invoke the onClosed callback after a short delay to ensure the session is closed
                onClosed?.let {
                    _rangingData.value = RangingSessionAction.OnStart
                    // Wait for a moment to ensure the session is properly closed before invoking the callback
                    // Launch a coroutine to delay and call onClosed
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000)
                        it()
                    }

                }
            }
        } catch (e: Exception) {
            _rangingData.value = RangingSessionAction.OnError(SessionClosedReason.UNKNOWN)
        }
    }

    private fun hasRangingPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RANGING
        ) == PackageManager.PERMISSION_GRANTED
    }

}

