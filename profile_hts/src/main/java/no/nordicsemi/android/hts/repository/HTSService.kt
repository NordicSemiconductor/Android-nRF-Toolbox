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
import no.nordicsemi.android.hts.data.BatteryLevelParser
import no.nordicsemi.android.hts.data.HTSDataParser
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
class HTSService : NotificationService() {

    @Inject
    lateinit var repository: HTSRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        repository.setServiceRunning(true)

        val device = repository.device

        device.serviceData?.let { discoverService(it, lifecycleScope) }

        repository.stopEvent
            .onEach {
                disconnect()
                stopIfDisconnected()
            }.launchIn(lifecycleScope)

        return START_REDELIVER_INTENT
    }

    private fun discoverService(remoteService: RemoteService, scope: CoroutineScope) =
        lifecycleScope.launch {
            remoteService.owner?.services()
                ?.onEach { services ->
                    handleServiceDiscovery(
                        services,
                        HTS_MEASUREMENT_CHARACTERISTIC_UUID,
                        ::handleHTSData
                    )
                    handleServiceDiscovery(
                        services,
                        BATTERY_LEVEL_CHARACTERISTIC_UUID,
                        ::handleBatteryLevel
                    )
                }
                ?.catch { e -> Timber.e(e) }
                ?.launchIn(scope)
        }

    private suspend fun handleServiceDiscovery(
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

    private suspend fun handleHTSData(characteristic: RemoteCharacteristic) {
        characteristic.subscribe()
            .mapNotNull { HTSDataParser.parse(it) }
            .onEach { htsData ->
                repository.onHTSDataChanged(htsData)
            }
            .catch { e -> Timber.e(e) }
            .launchIn(lifecycleScope)
    }

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

    private fun stopIfDisconnected() {
        stopSelf()
    }

    private suspend fun disconnect() {
        if (repository.peripheral?.isConnected == true) repository.peripheral?.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.setServiceRunning(false)
    }

}