package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.BodySensorLocationParser
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSData
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSDataParser
import no.nordicsemi.android.toolbox.libs.profile.repository.HRSRepository
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb")
private val HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

internal class HrsHandler : ProfileHandler() {
    override val profile: Profile = Profile.HRS
    private val _hrsData = MutableSharedFlow<HRSData>()
    private var _bodySensorLocation = MutableSharedFlow<Int>()

    override fun getNotification() = _hrsData.asSharedFlow()

    override fun readCharacteristic() = _bodySensorLocation.asSharedFlow()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        remoteService.characteristics.firstOrNull { it.uuid == HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { HRSDataParser.parse(it) }
            ?.onEach { data ->
                HRSRepository.updateHRSData(data)
            }
            ?.onCompletion { HRSRepository.clear() }
            ?.catch { e ->
                // Handle the error
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)

        remoteService.characteristics.firstOrNull { it.uuid == BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.read()
            ?.let { BodySensorLocationParser.parse(it) }
            ?.let { bodySensorLocation ->
                HRSRepository.updateBodySensorLocation(bodySensorLocation)
            }
    }
}
