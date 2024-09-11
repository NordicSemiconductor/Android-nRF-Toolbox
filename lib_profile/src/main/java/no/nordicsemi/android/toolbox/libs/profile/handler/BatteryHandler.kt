package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import java.util.UUID

val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")


class BatteryHandler : ProfileHandler() {
    override val profileModule: ProfileModule = ProfileModule.BATTERY
    override val connectionState: Flow<ConnectionState>
        get() = MutableSharedFlow()
    private val _batteryLevel = MutableSharedFlow<ByteArray>()
    override fun observeData() = _batteryLevel.asSharedFlow()

    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        remoteService.characteristics.firstOrNull { it.uuid == BATTERY_LEVEL_CHARACTERISTIC_UUID }
            ?.subscribe()
            ?.mapNotNull { it }
            ?.onEach { batteryLevel ->
                // Send the data to the repository
                _batteryLevel.emit(batteryLevel)
            }
            ?.catch { e ->
                Timber.e(e)
            }?.launchIn(scope)
    }
}
