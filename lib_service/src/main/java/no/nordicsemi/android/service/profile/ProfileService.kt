package no.nordicsemi.android.service.profile

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.log.timber.nRFLoggerTree
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.service.R
import no.nordicsemi.android.toolbox.profile.manager.ServiceManager
import no.nordicsemi.android.toolbox.profile.manager.ServiceManagerFactory
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.CentralManager.ConnectionOptions
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.WriteType
import timber.log.Timber
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

@AndroidEntryPoint
internal class ProfileService : NotificationService() {

    @Inject
    lateinit var centralManager: CentralManager
    private var logger: nRFLoggerTree? = null

    private val binder = LocalBinder()
    private val managedConnections = mutableMapOf<String, Job>()

    private val _devices = MutableStateFlow<Map<String, ServiceApi.DeviceData>>(emptyMap())
    private val _isMissingServices = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _disconnectionEvent = MutableStateFlow<ServiceApi.DisconnectionEvent?>(null)

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // Observe the Bluetooth state to handle global disconnection reasons.
        centralManager.state.onEach { state ->
            if (state == Manager.State.POWERED_OFF) {
                _disconnectionEvent.value = ServiceApi.DisconnectionEvent(
                    "all_devices", // Generic address
                    CustomReason(DisconnectReason.BLUETOOTH_OFF)
                )
                // Optionally disconnect all devices
                _devices.value.keys.forEach { disconnect(it) }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.getStringExtra(DEVICE_ADDRESS)?.let { address ->
            connect(address)
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        managedConnections.values.forEach { it.cancel() }
        uprootLogger()
        super.onDestroy()
    }

    private fun connect(address: String) {
        // Return if already managed to avoid multiple connection jobs.
        if (managedConnections.containsKey(address)) return

        initLogger(address) // Initialize logger for the new device.

        val peripheral = centralManager.getPeripheralById(address) ?: run {
            Timber.w("Peripheral with address $address not found.")
            return
        }

        val job = lifecycleScope.launch {
            // Launch the initial connection attempt.
            launch {
                try {
                    centralManager.connect(peripheral, options = ConnectionOptions.Direct())
                } catch (e: Exception) {
                    Timber.e(e, "Failed to connect to $address")
                }
            }

            // Observe connection state changes and react accordingly.
            observeConnectionState(peripheral)
        }

        managedConnections[address] = job
        job.invokeOnCompletion {
            // Clean up when the management coroutine is cancelled.
            handleDisconnection(address, "Job cancelled")
            managedConnections.remove(address)
            stopServiceIfNoDevices()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.observeConnectionState(peripheral: Peripheral) {
        peripheral.state
            .onEach { state ->
                _devices.update {
                    it + (peripheral.address to (it[peripheral.address]?.copy(connectionState = state)
                        ?: ServiceApi.DeviceData(peripheral, state)))
                }

                when (state) {
                    ConnectionState.Connected -> {
                        try {
                            discoverAndObserveServices(peripheral, this)
                        } catch (e: Exception) {
                            Timber.e(e, "Service discovery failed for ${peripheral.address}")
                        }

                    }

                    is ConnectionState.Disconnected -> {
                        Timber.tag("AAAA").d("Disconnected State: ${peripheral.address}")
                        val reason = state.reason ?: DisconnectReason.UNKNOWN
                        _disconnectionEvent.value =
                            ServiceApi.DisconnectionEvent(
                                peripheral.address,
                                StateReason(reason as ConnectionState.Disconnected.Reason)
                            )
                        _devices.update { it - peripheral.address }
                        Timber.tag("AAA").d("Devices after disconnection: ${_devices.value.keys}")
                        handleDisconnection(peripheral.address, reason.toString())
                    }

                    else -> {
                        // Handle connecting/disconnecting states if needed
                    }
                }
            }.launchIn(this)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun discoverAndObserveServices(
        peripheral: Peripheral,
        scope: CoroutineScope
    ) {
        peripheral
            .services()
            .onEach { service ->
                var isMissing: Boolean? = null
                service?.map { removeService ->
                    ServiceManagerFactory
                        .createServiceManager(removeService.uuid)
                        ?.also { manager ->
                            Timber.tag("AAA")
                                .d("Found ServiceManager for service ${removeService.uuid}")
                            isMissing = false
                            _devices.update {
                                it + (peripheral.address to it[peripheral.address]!!.copy(
                                    services = it[peripheral.address]?.services?.plus(
                                        manager
                                    ) ?: listOf(manager)
                                ))
                            }
//                            _isMissingServices.update { it - peripheral.address }
                            scope.launch { // Launch observation for each service.
                                observeService(peripheral, removeService, manager)
                            }
                        }
                }
                if (isMissing != false) {
                    Timber.tag("AAA").w("Peripheral ${peripheral.address} is missing services")
                    _isMissingServices.update { it + (peripheral.address to true) }
                } else {
                    _isMissingServices.update { it - peripheral.address }
                    // If all required services are found, log it.
                    Timber.tag("AAA")
                        .d("Peripheral ${peripheral.address} has all required services")
                }
            }.launchIn(scope)

    }


    private suspend fun observeService(
        peripheral: Peripheral,
        service: RemoteService,
        manager: ServiceManager
    ) {
        try {
//            if (manager.requiresBonding(service.uuid) && peripheral.bondingState != BondState.BONDED) {
//                peripheral.ensureBonded()
//            }
            manager.observeServiceInteractions(peripheral.address, service, lifecycleScope)
        } catch (e: Exception) {
            Timber.tag("ObserveServices").e(e)
        }
    }

    private fun disconnect(address: String) {
        centralManager.getPeripheralById(address)?.let { peripheral ->
            lifecycleScope.launch {
                try {
                    peripheral.disconnect()
                    handleDisconnection(address, "Disconnected by user")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to disconnect from $address")
                }
            }
        }
        managedConnections[address]?.cancel()
    }

    private fun handleDisconnection(address: String, reason: String) {
        Timber.d("Handling disconnection for $address, reason: $reason")
        _devices.update { it - address }
        Timber.tag("AAA").d("Devices after disconnection: ${_devices.value.keys}")
        _isMissingServices.update { it - address }
    }

    private fun stopServiceIfNoDevices() {
        if (_devices.value.isEmpty()) {
            stopForegroundService()
            stopSelf()
        }
    }

    // Logger and other helper functions remain largely the same.
    private fun initLogger(deviceAddress: String) {
        if (logger != null) return
        logger = nRFLoggerTree(this, getString(R.string.app_name), deviceAddress)
            .also { Timber.plant(it) }
    }

    private fun uprootLogger() {
        logger?.let { Timber.uproot(it) }
        logger = null
    }

    // The Binder providing the public API.
    inner class LocalBinder : Binder(), ServiceApi {
        override val devices: StateFlow<Map<String, ServiceApi.DeviceData>>
            get() = _devices.asStateFlow()

        override val isMissingServices: StateFlow<Map<String, Boolean>>
            get() = _isMissingServices.asStateFlow()

        override val disconnectionEvent: StateFlow<ServiceApi.DisconnectionEvent?>
            get() = _disconnectionEvent.asStateFlow()

        override fun disconnect(address: String) = this@ProfileService.disconnect(address)

        override fun getPeripheral(address: String?): Peripheral? =
            address?.let { centralManager.getPeripheralById(it) }

        override suspend fun getMaxWriteValue(address: String, writeType: WriteType): Int? {
            val peripheral = getPeripheral(address) ?: return null
            if (peripheral.state.value != ConnectionState.Connected) return null

            return try {
                peripheral.requestHighestValueLength() // Request highest possible MTU
                peripheral.requestConnectionPriority(ConnectionPriority.HIGH)
                peripheral.readPhy()
                peripheral.maximumWriteValueLength(writeType)
            } catch (e: Exception) {
                Timber.e(e, "Failed to configure MTU for $address")
                null
            }
        }

        override suspend fun createBond(address: String) {
            getPeripheral(address)?.ensureBonded()
        }
    }
}

// Helper extension function for bonding
private suspend fun Peripheral.ensureBonded() {
    if (this.bondState.value == BondState.BONDED) return
    // Create bond and wait until bonded.
    createBond()
}