package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.battery.BatteryLevelParser
import no.nordicsemi.android.toolbox.libs.profile.repository.BatteryRepository
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class BatteryHandler : ProfileHandler() {
    override val profile: Profile = Profile.BATTERY

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun handleServices(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        remoteService.characteristics.firstOrNull { it.uuid == BATTERY_LEVEL_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { BatteryLevelParser.parse(it) }
            ?.onEach { batteryLevel ->
                BatteryRepository.updateBatteryLevel(deviceId, batteryLevel)
            }
            ?.onCompletion { BatteryRepository.clear(deviceId) }
            ?.catch { e ->
                Timber.e(e)
            }?.launchIn(scope)
    }
}
