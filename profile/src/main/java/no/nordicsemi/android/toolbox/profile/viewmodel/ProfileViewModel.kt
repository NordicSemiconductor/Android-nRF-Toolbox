package no.nordicsemi.android.toolbox.profile.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.Link
import no.nordicsemi.android.analytics.ProfileOpenEvent
import no.nordicsemi.android.common.logger.LoggerLauncher
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.timber.nRFLoggerTree
import no.nordicsemi.android.service.profile.CustomReason
import no.nordicsemi.android.service.profile.ProfileServiceManager
import no.nordicsemi.android.service.profile.ServiceApi
import no.nordicsemi.android.service.profile.StateReason
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.repository.DeviceRepository
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

internal sealed interface ConnectionEvent {

    data class OnRetryClicked(val device: String) : ConnectionEvent

    data object NavigateUp : ConnectionEvent

    data class DisconnectEvent(val device: String) : ConnectionEvent

    data object OpenLoggerEvent : ConnectionEvent
}

@HiltViewModel
internal class ConnectionViewModel @Inject constructor(
    private val profileServiceManager: ProfileServiceManager,
    private val navigator: Navigator,
    private val deviceRepository: DeviceRepository,
    private val analytics: AppAnalytics,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    val address: String = parameterOf(ProfileDestinationId)
    private val _deviceState = MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.Idle)
    val deviceState = _deviceState.asStateFlow()

    private var logger: nRFLoggerTree? = null
    private var serviceApi: WeakReference<ServiceApi>? = null
    private var peripheral: Peripheral? = null
    private var job: Job? = null

    init {
        connectToPeripheral(address)
        observeConnectedDevices()
        initLogger()
    }

    private suspend fun getServiceApi(): ServiceApi? {
        if (serviceApi == null) {
            serviceApi = WeakReference(profileServiceManager.bindService())
        }
        return serviceApi?.get()
    }

    private fun initLogger() {
        logger = nRFLoggerTree(context, address, context.getString(R.string.app_name)).also {
            Timber.plant(it)
        }
    }

    private fun observeConnectedDevices() = viewModelScope.launch {
        getServiceApi()?.let { api ->
            peripheral = api.getPeripheralById(address)

            api.connectedDevices
                .onEach { peripheralProfileMap ->
                    deviceRepository.updateConnectedDevices(peripheralProfileMap)

                    peripheralProfileMap[peripheral?.address]?.let { pair ->
                        deviceRepository.updateProfilePeripheralPair(pair.first, pair.second)
                        _deviceState.update {
                            DeviceConnectionState.Connected(
                                DeviceData(
                                    peripheral = pair.first,
                                    peripheralProfileMap = mapOf(pair.first to pair.second),
                                )
                            )

                        }
                    }

                    // Send each profile handler to a shared flow that profile ViewModels can observe
                    peripheralProfileMap[peripheral?.address]?.second?.forEach { handler ->
                        deviceRepository.updateAnalytics(address, handler.profile)

                    }
                }.launchIn(viewModelScope)

            updateConnectionState(api, address, peripheral?.isConnected == true)
        }
    }


    /**
     * Connect to the peripheral with the given address. Before connecting, the service must be bound.
     * The service will be started if not already running.
     * @param deviceAddress the address of the peripheral to connect to.
     */
    private fun connectToPeripheral(deviceAddress: String) = viewModelScope.launch {
        // Connect to the peripheral
        getServiceApi()?.let {
            if (peripheral == null) peripheral = it.getPeripheralById(address)
            if (peripheral?.isConnected != true) {
                profileServiceManager.connectToPeripheral(deviceAddress)
            }
        }
    }


    /**
     * Update the service data, including connection state and peripheral data.
     * @param api the service API.
     * @param deviceAddress the address of the connected device.
     */
    private fun updateConnectionState(
        api: ServiceApi,
        deviceAddress: String,
        isAlreadyConnected: Boolean
    ) {
        // Drop the first default state (Closed) before connection.
        job = api.getConnectionState(deviceAddress)
            ?.drop(if (isAlreadyConnected) 0 else 1)
            ?.onEach { connectionState ->
                if (peripheral == null) peripheral = api.getPeripheralById(address)
                when (connectionState) {
                    ConnectionState.Connected -> {
                        _deviceState.update { currentState ->
                            val currentData =
                                (currentState as? DeviceConnectionState.Connected)?.data
                            DeviceConnectionState.Connected(
                                currentData?.copy(
                                    peripheral = peripheral
                                ) ?: DeviceData(peripheral = peripheral)
                            )
                        }
                            .apply { checkForMissingServices(api) }
                            .also {
                                // Request maximum MTU size for the connection.
                                val mtuSize = api.getMaxWriteValue(address)
                                _deviceState.update { currentState ->
                                    val currentData =
                                        (currentState as? DeviceConnectionState.Connected)?.data
                                    DeviceConnectionState.Connected(
                                        currentData?.copy(
                                            maxValueLength = mtuSize
                                        ) ?: DeviceData(
                                            peripheral = peripheral,
                                            maxValueLength = mtuSize
                                        )
                                    )
                                }
                            }
                    }

                    is ConnectionState.Disconnected -> {
                        _deviceState.update {
                            DeviceConnectionState.Disconnected(
                                peripheral,
                                StateReason(connectionState.reason)
                            )
                        }.also {
                            // Remove the analytics logged profiles for the disconnected device.
                            deviceRepository.removeLoggedProfile(deviceAddress)
                        }
                    }

                    ConnectionState.Closed -> {
                        unbindService()
                        api.disconnectionReason.onEach { reason ->
                            if (reason != null) {
                                _deviceState.update {
                                    DeviceConnectionState.Disconnected(peripheral, reason)
                                }
                            } else {
                                _deviceState.update {
                                    DeviceConnectionState.Disconnected(
                                        peripheral,
                                        CustomReason(DisconnectReason.UNKNOWN)
                                    )
                                }
                            }
                        }.launchIn(viewModelScope)
                        job?.cancel()
                    }

                    ConnectionState.Connecting -> {
                        _deviceState.update {
                            DeviceConnectionState.Connecting
                        }
                    }

                    ConnectionState.Disconnecting -> {
                        // Update the state to disconnecting.
                        _deviceState.update {
                            DeviceConnectionState.Disconnecting
                        }
                    }
                }
            }
            ?.onCompletion {
                job?.cancel()
                job = null
            }?.launchIn(viewModelScope)
    }

    /**
     * Check for missing services.
     */
    private fun checkForMissingServices(api: ServiceApi) =
        api.isMissingServices.onEach { isMissing ->
            (_deviceState.value as? DeviceConnectionState.Connected)?.let { connectedState ->
                _deviceState.update {
                    connectedState.copy(
                        data = connectedState.data.copy(isMissingServices = isMissing)
                    )
                }
            }
        }.launchIn(viewModelScope)


    /**
     * Unbind the service.
     */
    private fun unbindService() {
        serviceApi?.let { profileServiceManager.unbindService() }
        serviceApi = null
    }

    fun onConnectionEvent(event: ConnectionEvent) {
        when (event) {
            is ConnectionEvent.DisconnectEvent -> disconnect(event.device)
            ConnectionEvent.NavigateUp -> {
                // If the device is connected and missing services, disconnect it before navigating up.
                if ((_deviceState.value as? DeviceConnectionState.Connected)?.data?.isMissingServices == true) {
                    disconnect(address)
                }
                navigator.navigateUp()
            }

            is ConnectionEvent.OnRetryClicked -> reconnectDevice(event.device)
            ConnectionEvent.OpenLoggerEvent -> openLogger()
        }
    }

    /**
     * Disconnect the device with the given address and navigate back.
     * @param device the address of the device to disconnect.
     */
    private fun disconnect(device: String) = viewModelScope.launch {
        getServiceApi()?.disconnect(device)
        unbindService()
    }

    /**
     * Launch the logger activity.
     */
    private fun openLogger() {
        // Log the event in the analytics.
        analytics.logEvent(ProfileOpenEvent(Link.LOGGER))
        LoggerLauncher.launch(context, logger?.session as? LogSession)
    }

    /**
     * Reconnect to the device with the given address.
     *
     * @param deviceAddress the address of the device to reconnect to.
     */
    private fun reconnectDevice(deviceAddress: String) = viewModelScope.launch {
        getServiceApi()?.let {
            connectToPeripheral(deviceAddress)
            updateConnectionState(it, deviceAddress, false)
        }
    }

}
