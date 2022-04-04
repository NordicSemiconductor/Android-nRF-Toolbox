package no.nordicsemi.android.csc.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.csc.data.CSCManager
import no.nordicsemi.android.csc.data.WheelSize
import no.nordicsemi.android.logger.ToolboxLogger
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.ConnectingResult
import no.nordicsemi.android.service.ServiceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CSCRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager
) {
    private var manager: CSCManager? = null

    private val _data = MutableStateFlow<BleManagerResult<CSCData>>(ConnectingResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    fun launch(device: BluetoothDevice) {
        serviceManager.startService(CSCService::class.java, device)
    }

    fun start(device: BluetoothDevice, scope: CoroutineScope) {
        val manager = CSCManager(context, scope, ToolboxLogger(context, "CSC", device.address))
        this.manager = manager

        manager.dataHolder.status.onEach {
            _data.value = it
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }
    }

    fun setWheelSize(wheelSize: WheelSize) {
        manager?.setWheelSize(wheelSize)
    }

    private suspend fun CSCManager.start(device: BluetoothDevice) {
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
