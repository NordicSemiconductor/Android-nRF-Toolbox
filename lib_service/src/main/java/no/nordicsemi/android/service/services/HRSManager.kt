package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.repository.HRSRepository
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.hrs.BodySensorLocationParser
import no.nordicsemi.android.toolbox.libs.core.data.hrs.HRSDataParser
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb")
private val HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

internal class HRSManager : ServiceManager {
    override val profile: Profile = Profile.HRS

    @OptIn(ExperimentalUuidApi::class)
    override fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        scope.launch {
            remoteService.characteristics.firstOrNull { it.uuid == HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID.toKotlinUuid() }
                ?.subscribe()
                ?.mapNotNull { HRSDataParser.parse(it) }
                ?.onEach { data ->
                    HRSRepository.updateHRSData(deviceId, data)
                }
                ?.onCompletion { HRSRepository.clear(deviceId) }
                ?.catch { e ->
                    // Handle the error
                    e.printStackTrace()
                    Timber.e(e)
                }?.launchIn(scope)
        }

        scope.launch {
            remoteService.characteristics.firstOrNull { it.uuid == BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID.toKotlinUuid() }
                ?.read()
                ?.let { BodySensorLocationParser.parse(it) }
                ?.let { bodySensorLocation ->
                    HRSRepository.updateBodySensorLocation(deviceId, bodySensorLocation)
                }
        }
    }
}
