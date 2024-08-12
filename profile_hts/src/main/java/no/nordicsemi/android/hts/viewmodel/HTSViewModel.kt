package no.nordicsemi.android.hts.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.hts.data.BatteryLevelParser
import no.nordicsemi.android.hts.data.HTSDataParser
import no.nordicsemi.android.hts.data.HTSServiceData
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

private val HTS_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID =
    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

/**
 * ViewModel for the Health Thermometer Service.
 */
@HiltViewModel
internal class HTSViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val htsParam = parameterOf(HTSDestinationId).remoteService
    private val _state: MutableStateFlow<HTSServiceData> =
        MutableStateFlow(HTSServiceData())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            htsParam
                .serviceData?.let { remoteService ->
                    discoverService(remoteService)
                }
            htsParam.connectionState
                ?.onEach {
                    _state.value = _state.value.copy(connectionState = it)
                }
                ?.launchIn(viewModelScope)
        }
    }

    fun onEvent(event: HTSScreenViewEvent) {
        when (event) {
            is NavigateUp, is DisconnectEvent -> {
                viewModelScope.launch {
                    // Navigate back
                    try {
                        disconnect(htsParam.peripheral!!)
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                    navigator.navigateUp()
                }
            }

            OpenLoggerEvent -> {
                // Open the loggers screen.
            }

            is OnTemperatureUnitSelected -> {
                _state.value = _state.value.copy(
                    temperatureUnit = event.value
                )
            }
        }
    }

    private suspend fun discoverService(remoteService: RemoteService) {
        remoteService.owner?.services()
            ?.onEach { services ->
                handleServiceDiscovery(
                    services,
                    HTS_MEASUREMENT_CHARACTERISTIC_UUID,
                    ::handleHTSData
                )
                handleServiceDiscovery(
                    services,
                    BATTERY_LEVEL_CHARACTERISTIC_UUID,
                    ::handleBatteryLevel
                )
            }
            ?.catch { e -> Timber.e(e) }
            ?.launchIn(viewModelScope)
    }

    private suspend fun handleServiceDiscovery(
        services: List<RemoteService>,
        characteristicUuid: UUID,
        handleData: suspend (characteristic: RemoteCharacteristic) -> Unit
    ) {
        services.forEach { service ->
            service.characteristics.firstOrNull { it.uuid == characteristicUuid }?.let {
                handleData(it)
            }
        }
    }

    private suspend fun handleHTSData(characteristic: RemoteCharacteristic) {
        characteristic.subscribe()
            .mapNotNull { HTSDataParser.parse(it) }
            .onEach { htsData ->
                _state.value = _state.value.copy(
                    deviceName = characteristic.service.owner?.name,
                    data = htsData
                )
            }
            .catch { e -> Timber.e(e) }
            .launchIn(viewModelScope)
    }

    private suspend fun handleBatteryLevel(characteristic: RemoteCharacteristic) {
        characteristic.subscribe()
            .mapNotNull { BatteryLevelParser.parse(it) }
            .onEach { batteryLevel ->
                _state.value = _state.value.copy(
                    batteryLevel = batteryLevel
                )
            }
            .catch { e -> Timber.e(e) }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }


    private suspend fun disconnect(peripheral: Peripheral) {
        peripheral.disconnect()
    }

}