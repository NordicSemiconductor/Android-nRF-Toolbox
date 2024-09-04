package no.nordicsemi.android.toolbox.libs.profile.service

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.DEVICE_ADDRESS
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandlerFactory
import no.nordicsemi.android.toolbox.libs.profile.repository.ConnectionRepository
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import javax.inject.Inject

class NoServiceFound

@AndroidEntryPoint
internal class ConnectionService : NotificationService() {

    @Inject
    lateinit var repository: ConnectionRepository

    @Inject
    lateinit var centralManager: CentralManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val device = intent?.getStringExtra(DEVICE_ADDRESS)!!

        val peripheral = centralManager.getPeripheralById(device)

        peripheral?.let {
            connect(device = it, scope = lifecycleScope)
            observerPeripheralState(it, lifecycleScope)
        }

        repository.stopEvent.onEach { event ->
            // TODO: Handle the stop event for the peripheral.
            stopSelf()
        }.launchIn(lifecycleScope)

        return START_REDELIVER_INTENT
    }

    /**
     * Connects to the peripheral device and observe peripheral states.
     *
     * @param device The peripheral to connect to.
     * @param autoConnect If `true`, the connection will be established using the Auto Connect feature.
     */
    private fun connect(
        device: Peripheral,
        autoConnect: Boolean = false,
        scope: CoroutineScope
    ) {
        if (!device.isDisconnected) return
        try {
            scope.launch {
                centralManager.connect(
                    peripheral = device,
                    options = if (autoConnect) {
                        CentralManager.ConnectionOptions.AutoConnect
                    } else CentralManager.ConnectionOptions.Direct()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
            return
        }
    }

    private fun observerPeripheralState(
        peripheral: Peripheral,
        scope: CoroutineScope
    ) {
        peripheral.state.onEach { state ->
            when (state) {
                ConnectionState.Connected -> {
                    repository.onConnectionStatusChanged(peripheral, ConnectionState.Connected)
                    handleConnectedState(peripheral, scope)
                }

                ConnectionState.Connecting -> {
                    repository.onConnectionStatusChanged(peripheral, ConnectionState.Connecting)
                }

                is ConnectionState.Disconnected -> {
                    repository.onConnectionStatusChanged(
                        peripheral,
                        ConnectionState.Disconnected(state.reason)
                    )
                    // Clean up when disconnected
                    repository.removeDisconnectedDevice(peripheral)
                    stopIfDisconnected()
                }

                ConnectionState.Disconnecting -> {
                    repository.onConnectionStatusChanged(peripheral, ConnectionState.Disconnecting)
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
                        collectProfileData(it) // Collect the data from the handler
                    }
                }
            }
            // Check if no service is found.
            if (handlers.isEmpty() && remoteServices.isNotEmpty()) {
                // No service found.
                repository.onProfileStateUpdated(peripheral, NoServiceFound())
                // Stop the service.
                stopIfDisconnected()
            }

            // Add the connected device and its handlers to the repository.
            if (handlers.isNotEmpty()) {
                repository.addConnectedDevice(peripheral, handlers)
            }
        }.launchIn(lifecycleScope)
    }

    /** Stop the service if the device is disconnected. */
    private fun stopIfDisconnected() {
        stopSelf()
    }

    private fun collectProfileData(handler: ProfileHandler) =
        handler.observeData().onEach {
            repository.onProfileDataReceived(it)
        }.launchIn(lifecycleScope)


}
