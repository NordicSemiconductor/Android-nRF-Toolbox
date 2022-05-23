package no.nordicsemi.android.bps.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.bps.data.BPSData
import no.nordicsemi.android.bps.data.BPSManager
import no.nordicsemi.android.logger.NordicLogger
import no.nordicsemi.android.logger.NordicLoggerFactory
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.theme.view.StringConst
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject

@ViewModelScoped
internal class BPSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val loggerFactory: NordicLoggerFactory,
    private val stringConst: StringConst
) {

    private var logger: NordicLogger? = null

    fun downloadData(scope: CoroutineScope, device: DiscoveredBluetoothDevice): Flow<BleManagerResult<BPSData>> = callbackFlow {
        val createdLogger = loggerFactory.create(stringConst.APP_NAME, "BPS", device.address()).also {
            logger = it
        }
        val manager = BPSManager(context, scope, createdLogger)

        manager.dataHolder.status.onEach {
            trySend(it)
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }

        awaitClose {
            manager.disconnect().enqueue()
            logger = null
        }
    }

    private suspend fun BPSManager.start(device: DiscoveredBluetoothDevice) {
        try {
            connect(device.device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openLogger() {
        logger?.openLogger()
    }

}
