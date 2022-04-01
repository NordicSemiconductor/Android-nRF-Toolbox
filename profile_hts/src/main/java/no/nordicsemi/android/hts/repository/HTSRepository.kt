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
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.ConnectingResult
import no.nordicsemi.android.service.ServiceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HTSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager
) {
    private var manager: HTSManager? = null

    private val _data = MutableStateFlow<BleManagerResult<HTSData>>(ConnectingResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    fun launch(device: BluetoothDevice) {
        serviceManager.startService(HTSService::class.java, device)
    }

    fun start(device: BluetoothDevice, scope: CoroutineScope) {
        val manager = HTSManager(context, scope, ToolboxLogger(context, "HTS"))
        this.manager = manager

        manager.dataHolder.status.onEach {
            _data.value = it
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }
    }

    private suspend fun HTSManager.start(device: BluetoothDevice) {
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
