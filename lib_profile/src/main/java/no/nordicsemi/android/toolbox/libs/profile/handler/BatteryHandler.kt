package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.profile.parser.BatteryLevelParser
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID

val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

class BatteryHandler {
    private val _batteryLevel = MutableSharedFlow<Int>()
    val batteryLevelData = _batteryLevel.asSharedFlow()

    suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        remoteService.characteristics.firstOrNull { it.uuid == BATTERY_LEVEL_CHARACTERISTIC_UUID }
            ?.subscribe()
            ?.mapNotNull { BatteryLevelParser.parse(it) }
            ?.onEach { batteryLevel ->
                // Send the data to the repository
                _batteryLevel.emit(batteryLevel)
            }
            ?.catch { e ->
                Timber.e(e)
            }?.launchIn(scope)
    }
}
