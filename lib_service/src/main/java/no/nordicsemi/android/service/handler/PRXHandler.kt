package no.nordicsemi.android.service.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.prx.AlarmLevelParser
import no.nordicsemi.android.service.repository.PRXRepository
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val LINK_LOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")

private val ALERT_LEVEL_CHARACTERISTIC_UUID =
    UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb")

internal class PRXHandler : ServiceHandler() {
    override val profile: Profile = Profile.PRX

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        val linkLossService = remoteService.includedServices
            .firstOrNull { it.uuid == LINK_LOSS_SERVICE_UUID.toKotlinUuid() }

        remoteService.characteristics.firstOrNull { it.uuid == ALERT_LEVEL_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { AlarmLevelParser.parse(it) }
            ?.onEach { PRXRepository.updatePRXData(deviceId, it) }
            ?.onCompletion { PRXRepository.clear(deviceId) }
            ?.catch { e ->
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)

        linkLossService?.characteristics?.firstOrNull { it.uuid == ALERT_LEVEL_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { AlarmLevelParser.parse(it) }
            ?.onEach { PRXRepository.updateLinkLossAlarmLevelData(deviceId, it) }
            ?.onCompletion { PRXRepository.clear(deviceId) }
            ?.catch { e ->
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)
    }

}
