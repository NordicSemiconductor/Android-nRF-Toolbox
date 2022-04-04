package no.nordicsemi.android.cgms.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.cgms.data.CGMData
import no.nordicsemi.android.cgms.data.CGMManager
import no.nordicsemi.android.logger.ToolboxLogger
import no.nordicsemi.android.logger.ToolboxLoggerFactory
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.ConnectingResult
import no.nordicsemi.android.service.ServiceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CGMRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val toolboxLoggerFactory: ToolboxLoggerFactory
) {
    private var manager: CGMManager? = null
    private var logger: ToolboxLogger? = null

    private val _data = MutableStateFlow<BleManagerResult<CGMData>>(ConnectingResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    fun launch(device: BluetoothDevice) {
        serviceManager.startService(CGMService::class.java, device)
    }

    fun start(device: BluetoothDevice, scope: CoroutineScope) {
        val createdLogger = toolboxLoggerFactory.create("CGMS", device.address).also {
            logger = it
        }
        val manager = CGMManager(context, scope, createdLogger)
        this.manager = manager

        manager.dataHolder.status.onEach {
            _data.value = it
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }
    }

    private suspend fun CGMManager.start(device: BluetoothDevice) {
        try {
            connect(device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun requestAllRecords() {
        manager?.requestAllRecords()
    }

    fun requestLastRecord() {
        manager?.requestLastRecord()
    }

    fun requestFirstRecord() {
        manager?.requestFirstRecord()
    }

    fun openLogger() {
        logger?.openLogger()
    }

    fun release() {
        manager?.disconnect()?.enqueue()
        logger = null
        manager = null
    }
}
