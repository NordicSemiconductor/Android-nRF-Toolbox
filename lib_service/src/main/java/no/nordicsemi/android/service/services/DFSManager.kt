package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.repository.DFSRepository
import no.nordicsemi.android.toolbox.lib.utils.logAndReport
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.azimuthal.AzimuthalMeasurementDataParser
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.controlPoint.ControlPointDataParser
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.ddf.DDFDataParser
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.DistanceMeasurementDataParser
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.DistanceMode
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.elevation.ElevationMeasurementDataParser
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.WriteType
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val DISTANCE_MEASUREMENT_CHARACTERISTIC_UUID =
    UUID.fromString("21490001-494a-4573-98af-f126af76f490")
private val AZIMUTH_MEASUREMENT_CHARACTERISTIC_UUID =
    UUID.fromString("21490002-494a-4573-98af-f126af76f490")
private val ELEVATION_MEASUREMENT_CHARACTERISTIC_UUID =
    UUID.fromString("21490003-494a-4573-98af-f126af76f490")
private val DDF_FEATURE_CHARACTERISTIC_UUID =
    UUID.fromString("21490004-494a-4573-98af-f126af76f490")
private val CONTROL_POINT_CHARACTERISTIC_UUID =
    UUID.fromString("21490005-494a-4573-98af-f126af76f490")

internal class DFSManager : ServiceManager {
    override val profile: Profile
        get() = Profile.DFS

    @OptIn(ExperimentalUuidApi::class)
    override fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == AZIMUTH_MEASUREMENT_CHARACTERISTIC_UUID.toKotlinUuid() }
                ?.subscribe()
                ?.mapNotNull { AzimuthalMeasurementDataParser().parse(it) }
                ?.onEach { DFSRepository.addNewAzimuth(deviceId, it) }
                ?.catch { it.logAndReport() }
                ?.onCompletion { DFSRepository.clear(deviceId) }
                ?.launchIn(scope)
        }

        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == DISTANCE_MEASUREMENT_CHARACTERISTIC_UUID.toKotlinUuid() }
                ?.subscribe()
                ?.mapNotNull { DistanceMeasurementDataParser().parse(it) }
                ?.onEach { DFSRepository.addNewDistance(deviceId, it) }
                ?.catch { it.logAndReport() }
                ?.onCompletion { DFSRepository.clear(deviceId) }
                ?.launchIn(scope)
        }

        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == ELEVATION_MEASUREMENT_CHARACTERISTIC_UUID.toKotlinUuid() }
                ?.subscribe()
                ?.mapNotNull { ElevationMeasurementDataParser().parse(it) }
                ?.onEach { DFSRepository.addNewElevation(deviceId, it) }
                ?.catch { it.logAndReport() }
                ?.onCompletion { DFSRepository.clear(deviceId) }
                ?.launchIn(scope)
        }

        // TODO: Verify read characteristics.
        scope.launch {
            val ddfFeatureCharacteristics = remoteService.characteristics
                .firstOrNull { it.uuid == DDF_FEATURE_CHARACTERISTIC_UUID.toKotlinUuid() }
            val isReadPropertyAvailable = ddfFeatureCharacteristics
                ?.properties?.contains(CharacteristicProperty.READ)
            if (isReadPropertyAvailable == true) {
                ddfFeatureCharacteristics.read()
                    .let { DDFDataParser().parse(it) }
                    ?.apply { DFSRepository.setAvailableDistanceModes(deviceId, this) }
            } else {
                Timber.e("Characteristic Property READ is not available for $ddfFeatureCharacteristics")
            }

        }

        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == CONTROL_POINT_CHARACTERISTIC_UUID.toKotlinUuid() }
                ?.apply { controlPointCharacteristic = this }
                ?.subscribe()
                ?.mapNotNull { ControlPointDataParser().parse(it) }
                ?.onEach { DFSRepository.onControlPointDataReceived(deviceId, it, scope) }
                ?.catch { it.logAndReport() }
                ?.onCompletion { DFSRepository.clear(deviceId) }
                ?.launchIn(scope)
        }
    }

    companion object {
        private lateinit var controlPointCharacteristic: RemoteCharacteristic

        private val MCPD_ENABLED_BYTES = byteArrayOf(0x01, 0x01)
        private val RTT_ENABLED_BYTES = byteArrayOf(0x01, 0x00)
        private val CHECK_CONFIG_BYTES = byteArrayOf(0x0A)

        suspend fun enableDistanceMode(deviceId: String, mode: DistanceMode) {
            val data = when (mode) {
                DistanceMode.MCPD -> MCPD_ENABLED_BYTES
                DistanceMode.RTT -> RTT_ENABLED_BYTES
            }

            writeOrSetStatusFailed(deviceId) {
                controlPointCharacteristic.write(data, WriteType.WITH_RESPONSE)
            }

        }

        suspend fun checkForCurrentDistanceMode(deviceId: String) {
            writeOrSetStatusFailed(deviceId) {
                controlPointCharacteristic.write(
                    CHECK_CONFIG_BYTES,
                    writeType = WriteType.WITH_RESPONSE
                )
            }
        }

        private suspend fun writeOrSetStatusFailed(
            deviceId: String,
            block: suspend () -> Unit
        ) {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
                DFSRepository.updateNewRequestStatus(deviceId, RequestStatus.FAILED)
            }
        }
    }

}
