package no.nordicsemi.android.toolbox.libs.profile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.profile.profile.hts.data.BatteryLevelParser
import no.nordicsemi.android.toolbox.libs.profile.profile.hts.data.HTSDataParser
import no.nordicsemi.android.toolbox.libs.profile.profile.hts.data.HtsData
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID

abstract class ProfileHandler {
    abstract fun observeData(): Flow<Any>
    abstract suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope)
}

class HrmHandler : ProfileHandler() {
    override fun observeData(): Flow<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        println("Subscribing to HRM")
    }
}

private val HTS_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb")

class HtsHandler : ProfileHandler() {

    private val _htsData = MutableStateFlow(HtsData())
    override fun observeData(): Flow<HtsData> = _htsData.asStateFlow()

    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        remoteService.characteristics.firstOrNull { it.uuid == HTS_MEASUREMENT_CHARACTERISTIC_UUID }
            ?.subscribe()
            ?.mapNotNull { HTSDataParser.parse(it) }
            ?.onEach { htsData ->
                _htsData.emit(htsData) // Emit the data to the flow
            }
            ?.catch { e ->
                Timber.e(e)
            }?.launchIn(scope)

    }
}

val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")


class BatteryHandler : ProfileHandler() {
    private val _batteryLevel = MutableStateFlow(0)
    override fun observeData(): Flow<Int> = _batteryLevel.asStateFlow()

    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        remoteService.characteristics.firstOrNull { it.uuid == BATTERY_LEVEL_CHARACTERISTIC_UUID }
            ?.subscribe()
            ?.mapNotNull { BatteryLevelParser.parse(it) }
            ?.onEach { batteryLevel ->
                // SEnd the data to the repository
                _batteryLevel.emit(batteryLevel)
            }
            ?.catch { e ->
                Timber.e(e)
            }?.launchIn(scope)
    }
}
