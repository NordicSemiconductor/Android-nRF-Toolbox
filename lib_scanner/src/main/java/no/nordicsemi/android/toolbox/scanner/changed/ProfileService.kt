package no.nordicsemi.android.toolbox.scanner.changed

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandlerFactory
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ProfileService : NotificationService() {

    @Inject
    lateinit var centralManager: CentralManager

    // Binder to expose methods to the client
    private val binder = LocalBinder()

    private val _connectedDevices =
        MutableStateFlow<Map<Peripheral, List<ProfileHandler>>>(emptyMap())

    inner class LocalBinder : Binder() {
        val connectedDevices
            get() = _connectedDevices.asStateFlow()

        // Connect device from the view model.
        fun connectPeripheral(deviceAddress: String, scope: CoroutineScope) {
            connectToPeripheral(deviceAddress, scope)
        }

        fun peripheralConnectionState(peripheralAddress: String): StateFlow<ConnectionState>? {
            return getPeripheralById(peripheralAddress)?.state
        }

        fun getPeripheralById(peripheralAddress: String): Peripheral? {
            return centralManager.getPeripheralById(peripheralAddress)
        }

        // Disconnect the peripheral
        suspend fun disconnectPeripheral(peripheralAddress: String) {
            val peripheral = centralManager.getPeripheralById(peripheralAddress)
            peripheral?.let {
                peripheral.disconnect()
                // Remove the device from the connected devices map
                val currentDevices = _connectedDevices.replayCache.firstOrNull() ?: emptyMap()
                currentDevices[peripheral]?.let {
                    val updatedDevices = currentDevices.toMutableMap().apply {
                        remove(peripheral)
                    }
                    _connectedDevices.tryEmit(updatedDevices)
                }
                // Stop the service if no device is connected
                if (_connectedDevices.replayCache.firstOrNull()?.isEmpty() == true) {
                    stopSelf()
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    // Public method to connect a peripheral (can be called from the client)
    fun connectToPeripheral(deviceAddress: String, scope: CoroutineScope) {
        val peripheral = centralManager.getPeripheralById(deviceAddress)
        peripheral?.let {
            connect(it, scope)
            observePeripheralState(it, scope)
        }
    }

    private fun connect(
        device: Peripheral,
        scope: CoroutineScope
    ) {
        // Similar to your existing connect function
        try {
            scope.launch {
                centralManager.connect(device)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
        }
    }

    private fun observePeripheralState(
        peripheral: Peripheral,
        scope: CoroutineScope
    ) {
        peripheral.state.onEach { state ->
            when (state) {
                ConnectionState.Connected -> {
                    // Handle the connected state
                    handleConnectedState(peripheral, scope)
                }

                ConnectionState.Connecting -> {
                }

                is ConnectionState.Disconnected -> {
                }

                ConnectionState.Disconnecting -> {
                }
            }
        }.launchIn(scope)
    }

    private fun handleConnectedState(
        peripheral: Peripheral,
        scope: CoroutineScope
    ) {
        peripheral.services().onEach { remoteServices ->
            val handlers = mutableListOf<ProfileHandler>()
            remoteServices.forEach { remoteService ->
                val handler = ProfileHandlerFactory.createHandler(remoteService.uuid)
                handler?.let {
                    handlers.add(it)
                    scope.launch {
                        it.handleServices(remoteService, scope)
                    }
                }
            }
            // Check if no service is found.
            if (handlers.isEmpty() && remoteServices.isNotEmpty()) {
                // No service found.
                //
                // Stop the service.
                stopSelf()
            }

            // Add the connected device and its handlers to the repository.
            if (handlers.isNotEmpty()) {
                updateConnectedDevices(peripheral, handlers)
            }
        }.launchIn(lifecycleScope)
    }

    private fun updateConnectedDevices(peripheral: Peripheral, handlers: List<ProfileHandler>) {
        val currentDevices = _connectedDevices.replayCache.firstOrNull() ?: emptyMap()
        val updatedDevices = currentDevices.toMutableMap().apply {
            this[peripheral] = handlers
        }
        _connectedDevices.tryEmit(updatedDevices) // Emit the updated device map
    }

}
