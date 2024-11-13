package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.repository.RSCSRepository
import no.nordicsemi.android.toolbox.lib.utils.logAndReport
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.rscs.RSCSDataParser
import no.nordicsemi.kotlin.ble.client.RemoteService
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val RSC_MEASUREMENT_CHARACTERISTIC_UUID =
    UUID.fromString("00002A53-0000-1000-8000-00805F9B34FB")

internal class RSCSManager : ServiceManager {
    override val profile: Profile
        get() = Profile.RSCS

    @OptIn(ExperimentalUuidApi::class)
    override fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == RSC_MEASUREMENT_CHARACTERISTIC_UUID.toKotlinUuid() }
                ?.subscribe()
                ?.mapNotNull { RSCSDataParser.parse(it) }
                ?.onEach { RSCSRepository.onRSCSDataChanged(deviceId, it) }
                ?.catch { it.logAndReport() }
                ?.onCompletion { RSCSRepository.clear(deviceId) }
                ?.launchIn(scope)
        }
    }
}