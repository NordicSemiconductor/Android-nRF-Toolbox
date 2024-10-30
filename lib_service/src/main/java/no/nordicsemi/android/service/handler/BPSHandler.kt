package no.nordicsemi.android.service.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.core.data.Profile
import no.nordicsemi.android.toolbox.libs.core.data.bps.BloodPressureMeasurementParser
import no.nordicsemi.android.toolbox.libs.core.data.bps.IntermediateCuffPressureParser
import no.nordicsemi.android.toolbox.libs.core.repository.BPSRepository
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb")
private val ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb")

internal class BPSHandler : ProfileHandler() {
    override val profile: Profile = Profile.BPS

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun handleServices(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        // Collect updates from both characteristics
        remoteService.characteristics.firstOrNull { it.uuid == BPM_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { BloodPressureMeasurementParser.parse(it) }
            ?.onEach { BPSRepository.updateBPSData(deviceId, it) }
            ?.onCompletion { BPSRepository.clear(deviceId) }
            ?.catch { e ->
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)

        remoteService.characteristics.firstOrNull { it.uuid == ICP_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { IntermediateCuffPressureParser.parse(it) }
            ?.onEach { BPSRepository.updateICPData(deviceId, it) }
            ?.onCompletion { BPSRepository.clear(deviceId) }
            ?.catch { e ->
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)
    }
}
