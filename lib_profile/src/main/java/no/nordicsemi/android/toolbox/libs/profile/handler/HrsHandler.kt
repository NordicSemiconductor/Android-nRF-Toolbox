package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSData
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSDataParser
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import java.util.UUID

private val BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb")
private val HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

class HrsHandler : ProfileHandler() {
    override val profile: Profile = Profile.HRS
    override val connectionState: Flow<ConnectionState>
        get() = MutableSharedFlow()

    private val _hrsData = MutableSharedFlow<HRSData>()
    override fun observeData() = _hrsData.asSharedFlow()

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
    }
}
