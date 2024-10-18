package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.prx.AlarmLevel
import no.nordicsemi.android.toolbox.libs.profile.data.prx.AlarmLevelParser
import no.nordicsemi.android.toolbox.libs.profile.data.prx.PRXData
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val LINK_LOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")

private val ALERT_LEVEL_CHARACTERISTIC_UUID =
    UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb")

internal class PRXHandler : ProfileHandler() {
    override val profile: Profile
        get() = Profile.PRX
    private val _linkLossAlarmLevel = MutableSharedFlow<AlarmLevel>()
    private val _prxCharacteristic = MutableSharedFlow<AlarmLevel>()
    private val _prxData = MutableStateFlow<PRXData?>(null)

    override fun getNotification(): Flow<PRXData> =
        _prxData.filterNotNull()

    override fun readCharacteristic(): Flow<Nothing>? = null

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        val linkLossService = remoteService.includedServices
            .firstOrNull { it.uuid == LINK_LOSS_SERVICE_UUID.toKotlinUuid() }

        remoteService.characteristics.firstOrNull { it.uuid == ALERT_LEVEL_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { AlarmLevelParser.parse(it) }
            ?.onEach { alarmLevel ->
                // Send the data to the repository
                _prxCharacteristic.emit(alarmLevel)
                _prxData.value = _prxData.value?.copy(localAlarmLevel = alarmLevel)
                    ?: PRXData(localAlarmLevel = alarmLevel)
            }?.catch { e ->
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)

        linkLossService?.characteristics?.firstOrNull { it.uuid == ALERT_LEVEL_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { AlarmLevelParser.parse(it) }
            ?.onEach { alarmLevel ->
                // Send the data to the repository
                _linkLossAlarmLevel.emit(alarmLevel)
                _prxData.value = _prxData.value?.copy(linkLossAlarmLevel = alarmLevel)
                    ?: PRXData(linkLossAlarmLevel = alarmLevel)
            }?.catch { e ->
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)
    }

}
