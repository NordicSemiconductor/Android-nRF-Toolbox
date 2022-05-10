package no.nordicsemi.android.hts.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.hts.data.HTSData
import no.nordicsemi.android.hts.data.HTSManager
import no.nordicsemi.android.logger.ToolboxLogger
import no.nordicsemi.android.logger.ToolboxLoggerFactory
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.ConnectingResult
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HTSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val toolboxLoggerFactory: ToolboxLoggerFactory
) {
    private var manager: HTSManager? = null
    private var logger: ToolboxLogger? = null

    private val _data = MutableStateFlow<BleManagerResult<HTSData>>(ConnectingResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    fun launch(device: DiscoveredBluetoothDevice) {
        serviceManager.startService(HTSService::class.java, device)
    }

    fun start(device: DiscoveredBluetoothDevice, scope: CoroutineScope) {
        val createdLogger = toolboxLoggerFactory.create("HTS", device.address()).also {
            logger = it
        }
        val manager = HTSManager(context, scope, createdLogger)
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

    private suspend fun HTSManager.start(device: DiscoveredBluetoothDevice) {
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
