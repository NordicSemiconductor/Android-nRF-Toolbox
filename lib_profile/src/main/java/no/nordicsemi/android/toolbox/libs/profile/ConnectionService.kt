package no.nordicsemi.android.toolbox.libs.profile

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.toolbox.libs.profile.spec.BATTERY_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.BPS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.CGMS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.CSC_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.GLS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.HRS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.HTS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.PRX_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.RSCS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.UART_SERVICE_UUID
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/** A set of supported service UUIDs. */
private val supportedServiceUuids = setOf(
    BPS_SERVICE_UUID, CSC_SERVICE_UUID, CGMS_SERVICE_UUID,
    GLS_SERVICE_UUID, HRS_SERVICE_UUID, PRX_SERVICE_UUID,
    RSCS_SERVICE_UUID, UART_SERVICE_UUID
)

@AndroidEntryPoint
internal class ConnectionService : NotificationService() {

    @Inject
    lateinit var repository: ConnectionRepository

    @Inject
    lateinit var centralManager: CentralManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)


        repository.apply {
            peripheral?.let {
                connect(device = it, scope = lifecycleScope)
                observerPeripheralState(it, lifecycleScope)
            }
        }

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
                    repository.onConnectionStatusChanged(ConnectionState.Connected)
                    handleConnectedState(peripheral, scope)
                }

                ConnectionState.Connecting -> {
                    repository.onConnectionStatusChanged(ConnectionState.Connecting)
                }

                is ConnectionState.Disconnected -> {
                    repository.onConnectionStatusChanged(ConnectionState.Disconnected(state.reason))
                    // Clean up when disconnected
                    removeDisconnectedDevice(peripheral)
                    stopIfDisconnected()
                }

                ConnectionState.Disconnecting -> {
                    repository.onConnectionStatusChanged(ConnectionState.Disconnecting)
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

            if (handlers.isNotEmpty()) {
                repository.addConnectedDevice(peripheral, handlers)
            }

            if (remoteServices.isNotEmpty()) {
                // Update the profile state to service not found.
                repository.onProfileStateUpdated(ProfileState.NoServiceFound)
                // Stop the service.
                stopIfDisconnected()
                // Disconnect the device.
            }
        }.launchIn(scope)
    }

    /** Stop the service if the device is disconnected. */
    private fun stopIfDisconnected() {
        stopSelf()
    }

    private suspend fun collectProfileData(handler: ProfileHandler) {
        handler.observeData().collect { data ->
            repository.onProfileDataReceived(data)
        }
    }
}


object ProfileHandlerFactory {
    fun createHandler(serviceUuid: UUID): ProfileHandler? {
        return when (serviceUuid) {
            HTS_SERVICE_UUID -> HtsHandler()
            BATTERY_SERVICE_UUID -> BatteryHandler()
            HRS_SERVICE_UUID -> HrmHandler()
            // Add more service handlers as needed
            else -> null
        }
    }
}
