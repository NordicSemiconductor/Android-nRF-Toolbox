package no.nordicsemi.android.service.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.core.data.Profile
import no.nordicsemi.android.toolbox.libs.core.data.hts.HTSDataParser
import no.nordicsemi.android.toolbox.libs.core.repository.HTSRepository
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val HTS_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb")

internal class HtsHandler : ServiceHandler() {
    override val profile: Profile = Profile.HTS

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        remoteService.characteristics.firstOrNull { it.uuid == HTS_MEASUREMENT_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { HTSDataParser.parse(it) }
            ?.onEach { htsData ->
                HTSRepository.updateHTSData(deviceId, htsData)
            }
            ?.onCompletion { HTSRepository.clear(deviceId) }
            ?.catch { e ->
                Timber.e(e)
            }?.launchIn(scope)

    }
}