package no.nordicsemi.android.gls.repository

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
import no.nordicsemi.android.gls.data.GLSData
import no.nordicsemi.android.gls.data.GLSManager
import no.nordicsemi.android.gls.data.WorkingMode
import no.nordicsemi.android.logger.NordicLogger
import no.nordicsemi.android.logger.NordicLoggerFactory
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.theme.view.StringConst
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject

@ViewModelScoped
internal class GLSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val loggerFactory: NordicLoggerFactory,
    private val stringConst: StringConst
) {

    private var manager: GLSManager? = null
    private var logger: NordicLogger? = null

    fun downloadData(scope: CoroutineScope, device: DiscoveredBluetoothDevice): Flow<BleManagerResult<GLSData>> = callbackFlow {
        val createdLogger = loggerFactory.create(stringConst.APP_NAME, "GLS", device.address()).also {
            logger = it
        }
        val managerInstance = manager ?: GLSManager(context, scope, createdLogger)
        manager = managerInstance

        managerInstance.dataHolder.status.onEach {
            send(it)
        }.launchIn(scope)

        scope.launch {
            managerInstance.start(device)
        }

        awaitClose {
            launch {
                manager?.disconnect()?.suspend()
                logger = null
                manager = null
            }
        }
    }

    private suspend fun GLSManager.start(device: DiscoveredBluetoothDevice) {
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

    fun requestMode(workingMode: WorkingMode) {
        when (workingMode) {
            WorkingMode.ALL -> manager?.requestAllRecords()
            WorkingMode.LAST -> manager?.requestLastRecord()
            WorkingMode.FIRST -> manager?.requestFirstRecord()
        }.exhaustive
    }
}
