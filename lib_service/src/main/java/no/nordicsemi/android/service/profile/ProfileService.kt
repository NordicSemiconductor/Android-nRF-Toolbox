package no.nordicsemi.android.service.profile

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.log.timber.nRFLoggerTree
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.service.R
import no.nordicsemi.android.service.services.ServiceManager
import no.nordicsemi.android.toolbox.lib.utils.spec.CGMS_SERVICE_UUID
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.CentralManager.ConnectionOptions
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyOption
import no.nordicsemi.kotlin.ble.core.WriteType
import timber.log.Timber
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

@AndroidEntryPoint
internal class ProfileService : NotificationService() {

    @Inject
    lateinit var centralManager: CentralManager
    private var logger: nRFLoggerTree? = null
    private val binder = LocalBinder()

    private val _connectedDevices =
        MutableStateFlow<Map<String, Pair<Peripheral, List<ServiceManager>>>>(emptyMap())
    private val _isMissingServices = MutableStateFlow(false)
    private val _disconnectionReason = MutableStateFlow<DeviceDisconnectionReason?>(null)

    private var connectionJob: Job? = null
    private var serviceHandlingJob: Job? = null

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // Observe the Bluetooth state
        centralManager.state.onEach { state ->
            if (state == Manager.State.POWERED_OFF) {
                _disconnectionReason.tryEmit(CustomReason(DisconnectReason.BLUETOOTH_OFF))
            }
        }.launchIn(lifecycleScope)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.getStringExtra(DEVICE_ADDRESS)?.let { deviceAddress ->
            initLogger(deviceAddress)
            initiateConnection(deviceAddress)
        }
        return START_REDELIVER_INTENT
    }

    inner class LocalBinder : Binder(), ServiceApi {
        override val connectedDevices: Flow<Map<String, Pair<Peripheral, List<ServiceManager>>>>
            get() = _connectedDevices.asSharedFlow()

        override val isMissingServices: Flow<Boolean>
            get() = _isMissingServices.asStateFlow()

        override val disconnectionReason: Flow<DeviceDisconnectionReason?>
            get() = _disconnectionReason.asStateFlow()

        override suspend fun getMaxWriteValue(address: String, writeType: WriteType): Int? =
            getPeripheralById(address)?.let {
                if (it.isConnected) {
                    try {
                        it.requestHighestValueLength()
                        it.requestConnectionPriority(ConnectionPriority.HIGH)
                        it.setPreferredPhy(Phy.PHY_LE_2M, Phy.PHY_LE_2M, PhyOption.S2)
                    } catch (e: Exception) {
                        Timber.e("Could not change mtu size $e")
                    }
                }
                it.maximumWriteValueLength(writeType)
            }

        override suspend fun createBonding(address: String) {
            val peripheral = getPeripheralById(address)
            peripheral?.bondState
                ?.onEach { state ->
                    if (state == BondState.NONE) {
                        peripheral.createBond()
                    }
                }
                ?.filter { it == BondState.BONDED }
                ?.first() // suspend until bonded
        }

        override fun getPeripheralById(address: String?): Peripheral? =
            address?.let { centralManager.getPeripheralById(it) }

        override fun disconnect(deviceAddress: String) {
            lifecycleScope.launch {
                try {
                    centralManager.getPeripheralById(deviceAddress)
                        ?.let { peripheral ->
                            if (peripheral.isConnected) peripheral.disconnect()
                            handleDisconnection(deviceAddress)
                        }
                } catch (e: Exception) {
                    Timber.e(e, "Couldn't disconnect from the $deviceAddress")
                }
            }
        }

        override fun getConnectionState(address: String): Flow<ConnectionState>? {
            val peripheral = getPeripheralById(address) ?: return null
            return peripheral.state.also { stateFlow ->
                // Since the initial state is Gatt closed, drop the first state.
                connectionJob = stateFlow.drop(1).onEach { state ->
                    when (state) {
                        ConnectionState.Connected -> {
                            _isMissingServices.tryEmit(false)
                            // Discover services if not already discovered
                            if (_connectedDevices.value[address] == null) {
                                discoverServices(peripheral)
                            }
                        }

                        ConnectionState.Connecting -> _disconnectionReason.tryEmit(null)
                        is ConnectionState.Disconnected -> {
                            _disconnectionReason.tryEmit(StateReason(state.reason))
                        }

                        ConnectionState.Closed -> handleDisconnection(address)
                        else -> { /* Handle other states if necessary. */
                        }
                    }
                }.onCompletion { connectionJob?.cancel() }.launchIn(lifecycleScope)
            }
        }

    }

    /**
     * Connect to the peripheral and observe its state.
     */
    private fun initiateConnection(deviceAddress: String) {
        centralManager.getPeripheralById(deviceAddress)?.let { peripheral ->
            lifecycleScope.launch {
                connectPeripheral(peripheral)
            }
        }
    }

    private suspend fun connectPeripheral(peripheral: Peripheral) {
        runCatching {
            centralManager.connect(peripheral, options = ConnectionOptions.Direct())
        }.onFailure { exception ->
            Timber.e(exception, "Could not connect to the ${peripheral.address}")
            stopForegroundService() // Stop service if connection fails
        }
    }

    /**
     * Discover services and characteristics for the connected [peripheral].
     */
    @OptIn(ExperimentalUuidApi::class)
    private fun discoverServices(peripheral: Peripheral) {
        val discoveredServices = mutableListOf<ServiceManager>()
        serviceHandlingJob = peripheral.services().onEach { remoteServices ->
            remoteServices.forEach { remoteService ->
                val serviceManager = ServiceManagerFactory.createServiceManager(remoteService.uuid)
                serviceManager?.let { manager ->
                    Timber.tag("DiscoverServices").i("${manager.profile}")
                    discoveredServices.add(manager)
                    lifecycleScope.launch {
                        try {
                            val requiresBonding =
                                remoteService.uuid == CGMS_SERVICE_UUID.toKotlinUuid() && peripheral.hasBondInformation

                            if (requiresBonding) {
                                peripheral.bondState
                                    .onEach { state ->
                                        if (state == BondState.NONE) {
                                            peripheral.createBond()
                                        }
                                    }
                                    .filter { it == BondState.BONDED }
                                    .first() // suspend until bonded
                            }

                            manager.observeServiceInteractions(
                                peripheral.address,
                                remoteService,
                                this
                            )
                        } catch (e: Exception) {
                            Timber.tag("ObserveServices").e(e)
                            handleDisconnection(peripheral.address)
                        }
                    }
                }
            }
            when {
                discoveredServices.isEmpty() -> {
                    if (remoteServices.isNotEmpty())
                        _isMissingServices.tryEmit(true)
                }

                peripheral.isConnected -> {
                    _isMissingServices.tryEmit(false)
                    updateConnectedDevices(peripheral, discoveredServices)
                }
            }
        }.onCompletion { serviceHandlingJob?.cancel() }.launchIn(lifecycleScope)
    }

    /**
     * Update the connected devices with the latest state.
     */
    private fun updateConnectedDevices(peripheral: Peripheral, handlers: List<ServiceManager>) {
        _connectedDevices.update {
            it.toMutableMap().apply { this[peripheral.address] = peripheral to handlers }
        }
    }

    /**
     * Handle disconnection and cleanup for the given peripheral.
     */
    private fun handleDisconnection(peripheral: String) {
        val currentDevices = _connectedDevices.value.toMutableMap()
        currentDevices[peripheral]?.let {
            currentDevices.remove(peripheral)
            _connectedDevices.tryEmit(currentDevices)
        }
        clearJobs()
        clearFlags()
        stopServiceIfNoDevices()
    }

    /**
     * Clear any active jobs for connection and service handling.
     */
    private fun clearJobs() {
        connectionJob?.cancel()
        serviceHandlingJob?.cancel()
    }

    /**
     * Stop the service if no devices are connected.
     */
    private fun stopServiceIfNoDevices() {
        if (_connectedDevices.value.isEmpty()) {
            stopForegroundService() //// Remove notification from the foreground service
            stopSelf() // Stop the service
        }
    }

    /**
     * Initialize the logger for the specified device.
     */
    private fun initLogger(device: String) {
        logger?.let { Timber.uproot(it) }
        logger = nRFLoggerTree(this, this.getString(R.string.app_name), device)
            .also { Timber.plant(it) }
    }

    /**
     * Uproot the logger and clear the logger instance.
     */
    private fun uprootLogger() {
        logger?.let { Timber.uproot(it) }
        logger = null
    }

    /**
     * Clear the missing services and battery level flags.
     */
    private fun clearFlags() {
        _isMissingServices.tryEmit(false)
        uprootLogger()
    }

}
