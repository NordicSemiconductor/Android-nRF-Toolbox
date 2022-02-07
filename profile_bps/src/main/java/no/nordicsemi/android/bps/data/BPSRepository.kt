package no.nordicsemi.android.bps.data

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.bps.repository.BPSManager
import no.nordicsemi.android.service.BleManagerResult
import javax.inject.Inject

@ViewModelScoped
internal class BPSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {

    fun downloadData(device: BluetoothDevice): Flow<BleManagerResult<BPSData>> = callbackFlow {
        val scope = this
        val manager = BPSManager(context, scope)

        manager.dataHolder.status.onEach {
            trySend(it)
        }.launchIn(scope)

        try {
            manager.connect(device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        awaitClose {
            launch {
                manager.disconnect().suspend()
            }
        }
    }
}
