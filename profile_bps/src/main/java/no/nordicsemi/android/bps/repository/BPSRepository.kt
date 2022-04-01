package no.nordicsemi.android.bps.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.bps.data.BPSData
import no.nordicsemi.android.bps.data.BPSManager
import no.nordicsemi.android.log.ToolboxLogger
import no.nordicsemi.android.service.BleManagerResult
import javax.inject.Inject

@ViewModelScoped
internal class BPSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val logger: ToolboxLogger
) {

    fun downloadData(device: BluetoothDevice): Flow<BleManagerResult<BPSData>> = callbackFlow {
        val scope = this
        val manager = BPSManager(context, scope, logger)

        manager.dataHolder.status.onEach {
            trySend(it)
        }.launchIn(scope)

        manager.connect(device)
            .useAutoConnect(false)
            .retry(3, 100)
            .enqueue()

        awaitClose {
            manager.disconnect().enqueue()
        }
    }
}
