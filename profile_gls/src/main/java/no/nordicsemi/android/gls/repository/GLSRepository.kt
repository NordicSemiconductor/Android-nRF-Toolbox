package no.nordicsemi.android.gls.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
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
import no.nordicsemi.android.log.ToolboxLogger
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@ViewModelScoped
internal class GLSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private var manager: GLSManager? = null

    fun downloadData(device: BluetoothDevice): Flow<BleManagerResult<GLSData>> = callbackFlow {
        val scope = this
        val managerInstance = manager ?: GLSManager(context, scope, ToolboxLogger(context, "GLS")).apply {
            try {
                connect(device)
                    .useAutoConnect(false)
                    .retry(3, 100)
                    .suspend()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        manager = managerInstance

        managerInstance.dataHolder.status.onEach {
            trySend(it)
        }.launchIn(scope)

        awaitClose {
            launch {
                manager?.disconnect()?.suspend()
                manager = null
            }
        }
    }

    fun requestMode(workingMode: WorkingMode) {
        when (workingMode) {
            WorkingMode.ALL -> manager?.requestAllRecords()
            WorkingMode.LAST -> manager?.requestLastRecord()
            WorkingMode.FIRST -> manager?.requestFirstRecord()
        }.exhaustive
    }
}
