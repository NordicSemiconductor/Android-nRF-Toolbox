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
import no.nordicsemi.android.toolbox.libs.profile.data.battery.BatteryLevelParser
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class BatteryHandler: ProfileHandler() {
    private val _batteryLevel = MutableSharedFlow<Int>()
    override val profile: Profile
        get() = Profile.BATTERY

    override fun getNotification() = _batteryLevel.asSharedFlow()

    override fun readCharacteristic(): Flow<Nothing>? = null

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        remoteService.characteristics.firstOrNull { it.uuid == BATTERY_LEVEL_CHARACTERISTIC_UUID.toKotlinUuid() }
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
