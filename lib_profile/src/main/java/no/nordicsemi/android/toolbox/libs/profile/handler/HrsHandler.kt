package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.BodySensorLocationParser
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSData
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSDataParser
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID

private val BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb")
private val HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

class HrsHandler : ProfileHandler<HRSData, Int>() {
    override val profile: Profile = Profile.HRS
    private val _hrsData = MutableSharedFlow<HRSData>()
    private var _bodySensorLocation: Int? = null

    override fun getNotification() = _hrsData.asSharedFlow()

    override fun readCharacteristic() = _bodySensorLocation

    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        remoteService.characteristics.firstOrNull { it.uuid == HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID }
            ?.subscribe()
            ?.mapNotNull { HRSDataParser.parse(it) }
            ?.onEach { data ->
                _hrsData.emit(data) // Emit the data to the flow
            }
            ?.catch { e ->
                // Handle the error
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)

        remoteService.characteristics.firstOrNull { it.uuid == BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID }
            ?.read()
            ?.let { BodySensorLocationParser.parse(it) }
            ?.let { bodySensorLocation ->
                // Handle the body sensor location
                _bodySensorLocation = bodySensorLocation

            }
    }
}
