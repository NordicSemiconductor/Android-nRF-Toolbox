package no.nordicsemi.android.cgms.data

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.cgms.repository.CGMManager
import no.nordicsemi.android.cgms.repository.CGMService
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.ConnectingResult
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CGMRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
) {
    private var manager: CGMManager? = null

    private val _data = MutableStateFlow<BleManagerResult<CGMData>>(ConnectingResult())
    val data = _data.asStateFlow()

    fun launch(device: BluetoothDevice) {
        serviceManager.startService(CGMService::class.java, device)
    }

    fun startManager(device: BluetoothDevice, scope: CoroutineScope) {
        val manager = CGMManager(context, scope)

        manager.dataHolder.status.onEach {
            _data.value = it
            Log.d("AAATESTAAA", "data: $it")
        }.launchIn(scope)

        manager.connect(device)
            .useAutoConnect(false)
            .retry(3, 100)
            .enqueue()
    }

    fun sendNewServiceCommand(workingMode: CGMServiceCommand) {
        when (workingMode) {
            CGMServiceCommand.REQUEST_ALL_RECORDS -> manager?.requestAllRecords()
            CGMServiceCommand.REQUEST_LAST_RECORD -> manager?.requestLastRecord()
            CGMServiceCommand.REQUEST_FIRST_RECORD -> manager?.requestFirstRecord()
            CGMServiceCommand.DISCONNECT -> release()
        }.exhaustive
    }

    private fun release() {
        serviceManager.stopService(CGMService::class.java)
        manager?.disconnect()?.enqueue()
        manager = null
    }
}
