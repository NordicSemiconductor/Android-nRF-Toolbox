package no.nordicsemi.android.uart.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.logger.ToolboxLogger
import no.nordicsemi.android.logger.ToolboxLoggerFactory
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.IdleResult
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.uart.data.*
import no.nordicsemi.android.utils.EMPTY
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UARTRepository @Inject internal constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val configurationDataSource: ConfigurationDataSource,
    private val toolboxLoggerFactory: ToolboxLoggerFactory,
) {
    private var manager: UARTManager? = null
    private var logger: ToolboxLogger? = null

    private val _data = MutableStateFlow<BleManagerResult<UARTData>>(IdleResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    val lastConfigurationName = configurationDataSource.lastConfigurationName

    fun launch(device: DiscoveredBluetoothDevice) {
        serviceManager.startService(UARTService::class.java, device)
    }

    fun start(device: DiscoveredBluetoothDevice, scope: CoroutineScope) {
        val createdLogger = toolboxLoggerFactory.create("UART", device.address()).also {
            logger = it
        }
        val manager = UARTManager(context, scope, createdLogger)
        this.manager = manager

        manager.dataHolder.status.onEach {
            _data.value = it
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }
    }

    fun sendText(text: String, newLineChar: MacroEol) {
        manager?.send(text.parseWithNewLineChar(newLineChar))
    }

    fun runMacro(macro: UARTMacro) {
        val command = macro.command?.parseWithNewLineChar(macro.newLineChar)
        manager?.send(command ?: String.EMPTY)
    }

    fun clearItems() {
        manager?.clearItems()
    }

    fun openLogger() {
        logger?.openLogger()
    }

    suspend fun saveConfigurationName(name: String) {
        configurationDataSource.saveConfigurationName(name)
    }

    private suspend fun UARTManager.start(device: DiscoveredBluetoothDevice) {
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
        manager = null
        logger = null
    }
}
