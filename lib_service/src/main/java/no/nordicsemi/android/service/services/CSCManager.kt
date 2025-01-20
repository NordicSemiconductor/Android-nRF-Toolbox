package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.repository.CSCRepository
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.csc.CSCDataParser
import no.nordicsemi.kotlin.ble.client.RemoteService
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val CSC_MEASUREMENT_CHARACTERISTIC_UUID =
    UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb")

internal class CSCManager : ServiceManager {
    override val profile: Profile
        get() = Profile.CSC

    @OptIn(ExperimentalUuidApi::class)
    override fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == CSC_MEASUREMENT_CHARACTERISTIC_UUID.toKotlinUuid() }
                ?.subscribe()
                ?.mapNotNull {
                    CSCDataParser.parse(it, CSCRepository.getData(deviceId).value.data.wheelSize)
                }
                ?.onEach { CSCRepository.onCSCDataChanged(deviceId, it) }
                ?.catch { it.printStackTrace() }
                ?.onCompletion { CSCRepository.clear(deviceId) }
                ?.launchIn(scope)
        }
    }

}
