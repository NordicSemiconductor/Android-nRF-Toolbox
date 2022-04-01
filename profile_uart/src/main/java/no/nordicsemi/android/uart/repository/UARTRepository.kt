package no.nordicsemi.android.uart.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.logger.ToolboxLogger
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.ConnectingResult
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.uart.data.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UARTRepository @Inject internal constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val configurationDataSource: ConfigurationDataSource
) {
    private var manager: UARTManager? = null

    private val _data = MutableStateFlow<BleManagerResult<UARTData>>(ConnectingResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    val lastConfigurationName = configurationDataSource.lastConfigurationName

    fun launch(device: BluetoothDevice) {
        serviceManager.startService(UARTService::class.java, device)
    }

    fun start(device: BluetoothDevice, scope: CoroutineScope) {
        val manager = UARTManager(context, scope, ToolboxLogger(context, "UART"))
        this.manager = manager

        manager.dataHolder.status.onEach {
            _data.value = it
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }
    }

    fun runMacro(macro: UARTMacro) {
        macro.command?.parseWithNewLineChar(macro.newLineChar)?.let {
            manager?.send(it)
        }
    }

    fun clearItems() {
        manager?.clearItems()
    }

    suspend fun saveConfigurationName(name: String) {
        configurationDataSource.saveConfigurationName(name)
    }

    private suspend fun UARTManager.start(device: BluetoothDevice) {
        try {
            connect(device)
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
    }
}
