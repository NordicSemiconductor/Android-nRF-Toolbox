package no.nordicsemi.android.toolbox.scanner.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import no.nordicsemi.kotlin.ble.core.util.distinct
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
internal class ScanningManager @Inject constructor(
    private val centralManager: CentralManager,
) {

    /**
     * Scans for BLE devices.
     */
    fun startScanning(): Flow<Peripheral> {
        return centralManager.scan(2000.milliseconds)
            .distinctByPeripheral()
            .map { it.peripheral }
            .distinct()
            .catch { e -> Timber.e(e) }
            .flowOn(Dispatchers.IO)
    }

}