package no.nordicsemi.android.hts.repository

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.toolbox.libs.profile.profile.hts.data.BatteryLevelParser
import no.nordicsemi.android.toolbox.libs.profile.profile.hts.data.HTSDataParser
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

private val HTS_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID =
    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@AndroidEntryPoint
internal class HTSService : NotificationService() {

    @Inject
    lateinit var repository: HTSRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        repository.apply {
            setServiceRunning(true)
            device.serviceData?.let { getRemoteServices(it, lifecycleScope) }
            stopEvent
                .onEach {
                    stopIfDisconnected()
                }.launchIn(lifecycleScope)
        }
        return START_REDELIVER_INTENT
    }

    /** Get the remote services and subscribe to the characteristics. */
    private fun getRemoteServices(remoteService: RemoteService, scope: CoroutineScope) =
        lifecycleScope.launch {
            remoteService.owner?.services()
                ?.onEach { services ->
                    findServiceByCharacteristic(
                        services,
                        HTS_MEASUREMENT_CHARACTERISTIC_UUID,
                        ::handleHTSData
                    )
                    findServiceByCharacteristic(
                        services,
                        BATTERY_LEVEL_CHARACTERISTIC_UUID,
                        ::handleBatteryLevel
                    )
                }
                ?.catch { e -> Timber.e(e) }
                ?.launchIn(scope)
        }

    /** Find the service by the characteristic UUID and subscribe to the characteristic. */
    private suspend fun findServiceByCharacteristic(
        services: List<RemoteService>,
        characteristicUuid: UUID,
        handleData: suspend (characteristic: RemoteCharacteristic) -> Unit
    ) {
        services.forEach { service ->
            service.characteristics.firstOrNull { it.uuid == characteristicUuid }?.let {
                handleData(it)
            }
        }
    }

    /** Subscribe to the HTS data characteristic. */
    private suspend fun handleHTSData(characteristic: RemoteCharacteristic) {
        characteristic.subscribe()
            .mapNotNull { HTSDataParser.parse(it) }
            .onEach { htsData ->
                repository.onHTSDataChanged(htsData)
            }
            .catch { e -> Timber.e(e) }
            .launchIn(lifecycleScope)
    }

    /** Subscribe to the battery level characteristic. */
    private suspend fun handleBatteryLevel(
        characteristic: RemoteCharacteristic
    ) {
        characteristic.subscribe()
            .mapNotNull { BatteryLevelParser.parse(it) }
            .onEach { batteryLevel ->
                repository.onBatteryLevelChanged(batteryLevel)
            }
            .catch { e ->
                e.printStackTrace()
                Timber.e(e)
            }
            .launchIn(lifecycleScope)
    }

    /** Stop the service if the device is disconnected. */
    private fun stopIfDisconnected() {
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.setServiceRunning(false)
    }

}