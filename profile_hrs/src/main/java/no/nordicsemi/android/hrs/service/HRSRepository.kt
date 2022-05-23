package no.nordicsemi.android.hrs.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.hrs.data.HRSData
import no.nordicsemi.android.hrs.data.HRSManager
import no.nordicsemi.android.logger.NordicLogger
import no.nordicsemi.android.logger.NordicLoggerFactory
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.IdleResult
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.theme.view.StringConst
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HRSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val loggerFactory: NordicLoggerFactory,
    private val stringConst: StringConst
) {
    private var manager: HRSManager? = null
    private var logger: NordicLogger? = null

    private val _data = MutableStateFlow<BleManagerResult<HRSData>>(IdleResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    fun launch(device: DiscoveredBluetoothDevice) {
        serviceManager.startService(HRSService::class.java, device)
    }

    fun start(device: DiscoveredBluetoothDevice, scope: CoroutineScope) {
        val createdLogger = loggerFactory.create(stringConst.APP_NAME, "HRS", device.address()).also {
            logger = it
        }
        val manager = HRSManager(context, scope, createdLogger)
        this.manager = manager

        manager.dataHolder.status.onEach {
            _data.value = it
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }
    }

    fun openLogger() {
        logger?.openLogger()
    }

    private suspend fun HRSManager.start(device: DiscoveredBluetoothDevice) {
        try {
            connect(device.device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        manager?.disconnect()?.enqueue()
        logger = null
        manager = null
    }
}
