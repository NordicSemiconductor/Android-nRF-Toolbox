package no.nordicsemi.android.bps.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.bps.data.BPSData
import no.nordicsemi.android.bps.data.BPSManager
import no.nordicsemi.android.logger.ToolboxLogger
import no.nordicsemi.android.logger.ToolboxLoggerFactory
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject

@ViewModelScoped
internal class BPSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val toolboxLoggerFactory: ToolboxLoggerFactory
) {

    private var logger: ToolboxLogger? = null

    fun downloadData(device: DiscoveredBluetoothDevice): Flow<BleManagerResult<BPSData>> = callbackFlow {
        val scope = this
        val createdLogger = toolboxLoggerFactory.create("BPS", device.address()).also {
            logger = it
        }
        val manager = BPSManager(context, scope, createdLogger)

        manager.dataHolder.status.onEach {
            trySend(it)
        }.launchIn(scope)

        manager.connect(device.device)
            .useAutoConnect(false)
            .retry(3, 100)
            .enqueue()

        awaitClose {
            manager.disconnect().enqueue()
            logger = null
        }
    }

    fun openLogger() {
        logger?.openLogger()
    }

}
