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
import no.nordicsemi.android.toolbox.libs.profile.data.hts.HTSDataParser
import no.nordicsemi.android.toolbox.libs.profile.data.hts.HtsData
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val HTS_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb")

internal class HtsHandler : ProfileHandler() {
    override val profile: Profile = Profile.HTS
    private val _htsData = MutableSharedFlow<HtsData>()

    override fun getNotification() = _htsData.asSharedFlow()

    override fun readCharacteristic(): Flow<Nothing>? = null

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        remoteService.characteristics.firstOrNull { it.uuid == HTS_MEASUREMENT_CHARACTERISTIC_UUID.toKotlinUuid() }
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